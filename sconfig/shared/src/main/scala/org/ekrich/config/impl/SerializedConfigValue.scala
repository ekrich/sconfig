/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.Externalizable
import java.io.IOException
import java.io.NotSerializableException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.ObjectStreamException
import java.{lang => jl}
import java.{util => ju}

import scala.jdk.CollectionConverters._
import scala.util.control.Breaks._

import org.ekrich.config.Config
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigList
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigValue
import org.ekrich.config.ConfigValueType

/**
 * Deliberately shoving all the serialization code into this class instead of
 * doing it OO-style with each subclass. Seems better to have it all in one
 * place. This class implements a lame serialization format that supports
 * skipping unknown fields, so it's moderately more extensible than the default
 * Java serialization format.
 */
// this is the version used by Java serialization, if it increments it's
// essentially an ABI break and bad
@SerialVersionUID(1L)
object SerializedConfigValue {
  private[impl] class FieldOut private[impl] (val code: SerializedField) {
    final private[impl] var bytes = new ByteArrayOutputStream
    final private[impl] var data = new DataOutputStream(bytes)
  }

  // this is a separate function to prevent bugs writing to the
  // outer stream instead of field.data
  @throws[IOException]
  private def writeOriginField(
      out: DataOutput,
      code: SerializedField,
      v: AnyRef
  ): Unit = {
    import SerializedField._
    code match {
      case ORIGIN_DESCRIPTION     => out.writeUTF(v.asInstanceOf[String])
      case ORIGIN_LINE_NUMBER     => out.writeInt(v.asInstanceOf[jl.Integer])
      case ORIGIN_END_LINE_NUMBER => out.writeInt(v.asInstanceOf[jl.Integer])
      case ORIGIN_TYPE            => out.writeByte(v.asInstanceOf[jl.Integer])
      case ORIGIN_URL             => out.writeUTF(v.asInstanceOf[String])
      case ORIGIN_RESOURCE        => out.writeUTF(v.asInstanceOf[String])
      case ORIGIN_COMMENTS =>
        val list = v.asInstanceOf[ju.List[String]]
        out.writeInt(list.size)
        for (s <- list.asScala) {
          out.writeUTF(s)
        }
      case ORIGIN_NULL_URL      => () // FALL THRU
      case ORIGIN_NULL_RESOURCE => () // FALL THRU
      case ORIGIN_NULL_COMMENTS =>
        () // nothing to write out besides code and length
      case _ =>
        throw new IOException("Unhandled field from origin: " + code)
    }
  }
  // not private because we use it to serialize ConfigException
  @throws[IOException]
  private[impl] def writeOrigin(
      out: DataOutput,
      origin: SimpleConfigOrigin,
      baseOrigin: SimpleConfigOrigin
  ): Unit = {
    var m: ju.Map[SerializedField, AnyRef] = null
    // to serialize a null origin, we write out no fields at all
    if (origin != null)
      m = origin.toFieldsDelta(baseOrigin)
    else
      m = ju.Collections.emptyMap[SerializedField, AnyRef]
    for (e <- m.entrySet.asScala) {
      val field = new FieldOut(e.getKey)
      val v = e.getValue
      writeOriginField(field.data, field.code, v)
      writeField(out, field)
    }
    writeEndMarker(out)
  }

  // not private because we use it to deserialize ConfigException
  @throws[IOException]
  private[impl] def readOrigin(
      in: DataInput,
      baseOrigin: SimpleConfigOrigin
  ): SimpleConfigOrigin = {
    import SerializedField._
    val m: ju.Map[SerializedField, AnyRef] = new ju.HashMap
    breakable {
      while (true) {
        val field: SerializedField = readCode(in)
        val v: AnyRef = field match {
          case END_MARKER =>
            break() // break - was return SimpleConfigOrigin.fromBase(baseOrigin, m)
          case ORIGIN_DESCRIPTION =>
            in.readInt // discard length - same for cases below
            in.readUTF
          case ORIGIN_LINE_NUMBER =>
            in.readInt
            in.readInt.asInstanceOf[jl.Integer]
          case ORIGIN_END_LINE_NUMBER =>
            in.readInt
            in.readInt.asInstanceOf[jl.Integer]
          case ORIGIN_TYPE =>
            in.readInt
            in.readUnsignedByte.asInstanceOf[jl.Integer]
          case ORIGIN_URL =>
            in.readInt
            in.readUTF
          case ORIGIN_RESOURCE =>
            in.readInt
            in.readUTF
          case ORIGIN_COMMENTS =>
            in.readInt
            val size = in.readInt
            val list = new ju.ArrayList[String](size)
            var i = 0
            while (i < size) {
              list.add(in.readUTF)
              i += 1
            }
            list
          case ORIGIN_NULL_URL | ORIGIN_NULL_RESOURCE | ORIGIN_NULL_COMMENTS =>
            // nothing to read besides code and length
            in.readInt
            "" // just something non-null to put in the map
          case ROOT_VALUE | ROOT_WAS_CONFIG | VALUE_DATA | VALUE_ORIGIN =>
            throw new IOException("Not expecting this field here: " + field)
          case UNKNOWN =>
            // skip unknown field
            skipField(in)
            null
        }
        if (v != null) m.put(field, v)
      }
    }
    SimpleConfigOrigin.fromBase(baseOrigin, m) // from break above
  }

  @throws[IOException]
  private def writeValueData(out: DataOutput, value: ConfigValue): Unit = {
    import SerializedValueType._
    val st = SerializedValueType.forValue(value)
    out.writeByte(st.ordinal)
    st match {
      case BOOLEAN =>
        out.writeBoolean(value.asInstanceOf[ConfigBoolean].unwrapped)
      case NULL => ()
      case INT  =>
        // saving numbers as both string and binary is redundant but easy
        out.writeInt(value.asInstanceOf[ConfigInt].unwrapped)
        out.writeUTF(value.asInstanceOf[ConfigNumber].transformToString)
      case LONG =>
        out.writeLong(value.asInstanceOf[ConfigLong].unwrapped)
        out.writeUTF(value.asInstanceOf[ConfigNumber].transformToString)
      case DOUBLE =>
        out.writeDouble(value.asInstanceOf[ConfigDouble].unwrapped)
        out.writeUTF(value.asInstanceOf[ConfigNumber].transformToString)
      case STRING =>
        out.writeUTF(value.asInstanceOf[ConfigString].unwrapped)
      case LIST =>
        val list = value.asInstanceOf[ConfigList]
        out.writeInt(list.size)
        for (v <- list.asScala) {
          writeValue(out, v, list.origin.asInstanceOf[SimpleConfigOrigin])
        }
      case OBJECT =>
        val obj = value.asInstanceOf[ConfigObject]
        out.writeInt(obj.size)
        for (e <- obj.entrySet.asScala) {
          out.writeUTF(e.getKey)
          writeValue(
            out,
            e.getValue,
            obj.origin.asInstanceOf[SimpleConfigOrigin]
          )
        }
      // Note: no default case
    }
  }
  @throws[IOException]
  private def readValueData(
      in: DataInput,
      origin: SimpleConfigOrigin
  ): AbstractConfigValue = {
    import SerializedValueType._
    val stb = in.readUnsignedByte
    val st = SerializedValueType.forInt(stb)

    st match {
      case BOOLEAN =>
        new ConfigBoolean(origin, in.readBoolean)
      case NULL =>
        new ConfigNull(origin)
      case INT =>
        val vi = in.readInt
        val si = in.readUTF
        new ConfigInt(origin, vi, si)
      case LONG =>
        val vl = in.readLong
        val sl = in.readUTF
        new ConfigLong(origin, vl, sl)
      case DOUBLE =>
        val vd = in.readDouble
        val sd = in.readUTF
        new ConfigDouble(origin, vd, sd)
      case STRING =>
        new ConfigString.Quoted(origin, in.readUTF)
      case LIST =>
        val listSize = in.readInt
        val list = new ju.ArrayList[AbstractConfigValue](listSize)
        var i = 0
        while (i < listSize) {
          val v = readValue(in, origin)
          list.add(v)
          i += 1
        }
        new SimpleConfigList(origin, list)
      case OBJECT =>
        val mapSize = in.readInt
        val map = new ju.HashMap[String, AbstractConfigValue](mapSize)
        var i = 0
        while (i < mapSize) {
          val key = in.readUTF
          val v = readValue(in, origin)
          map.put(key, v)
          i += 1
        }
        new SimpleConfigObject(origin, map)
    }
  }

  @throws[IOException]
  private def writeValue(
      out: DataOutput,
      value: ConfigValue,
      baseOrigin: SimpleConfigOrigin
  ): Unit = {
    val origin =
      new SerializedConfigValue.FieldOut(SerializedField.VALUE_ORIGIN)
    writeOrigin(
      origin.data,
      value.origin.asInstanceOf[SimpleConfigOrigin],
      baseOrigin
    )
    writeField(out, origin)
    val data = new SerializedConfigValue.FieldOut(SerializedField.VALUE_DATA)
    writeValueData(data.data, value)
    writeField(out, data)
    writeEndMarker(out)
  }
  @throws[IOException]
  private def readValue(
      in: DataInput,
      baseOrigin: SimpleConfigOrigin
  ): AbstractConfigValue = {
    var value: AbstractConfigValue = null
    var origin: SimpleConfigOrigin = null
    breakable {
      while (true) {
        val code = readCode(in)
        if (code eq SerializedField.END_MARKER) {
          if (value == null)
            throw new IOException(
              "No value data found in serialization of value"
            )
          break() // break - previous set value
        } else if (code eq SerializedField.VALUE_DATA) {
          if (origin == null)
            throw new IOException("Origin must be stored before value data")
          in.readInt
          value = readValueData(in, origin)
        } else if (code eq SerializedField.VALUE_ORIGIN) {
          in.readInt
          origin = readOrigin(in, baseOrigin)
        } else {
          // ignore unknown field
          skipField(in)
        }
      }
    }
    value // prior to break above
  }
  @throws[IOException]
  private def writeField(out: DataOutput, field: FieldOut): Unit = {
    val bytes = field.bytes.toByteArray
    out.writeByte(field.code.ordinal)
    out.writeInt(bytes.length)
    out.write(bytes)
  }
  @throws[IOException]
  private def writeEndMarker(out: DataOutput): Unit = {
    out.writeByte(SerializedField.END_MARKER.ordinal)
  }
  @throws[IOException]
  private def readCode(in: DataInput): SerializedField = {
    val c = in.readUnsignedByte
    if (c == SerializedField.UNKNOWN.ordinal)
      throw new IOException(
        "field code " + c + " is not supposed to be on the wire"
      )
    SerializedField.forInt(c)
  }
  @throws[IOException]
  private def skipField(in: DataInput): Unit = {
    val len = in.readInt
    // skipBytes doesn't have to block
    val skipped = in.skipBytes(len)
    if (skipped < len) {
      // wastefully use readFully() if skipBytes didn't work
      val bytes = new Array[Byte](len - skipped)
      in.readFully(bytes)
    }
  }
  private def shouldNotBeUsed =
    new ConfigException.BugOrBroken(
      classOf[SerializedConfigValue].getName +
        " should not exist outside of serialization"
    )
}

@SerialVersionUID(1L)
class SerializedConfigValue() // this has to be public for the Java deserializer
    extends AbstractConfigValue(null)
    with Externalizable {
  private var value: ConfigValue = null
  private var wasConfig: Boolean = false

  def this(value: ConfigValue) = {
    this()
    this.value = value
    this.wasConfig = false
  }

  def this(conf: Config) = {
    this(conf.root)
    this.wasConfig = true
  }
  // when Java deserializer reads this object, return the contained object instead.
  @throws[ObjectStreamException]
  private def readResolve(): jl.Object =
    if (wasConfig) value.asInstanceOf[ConfigObject].toConfig else value

  @throws[IOException]
  override def writeExternal(out: ObjectOutput): Unit = {
    if (value
          .asInstanceOf[AbstractConfigValue]
          .resolveStatus ne ResolveStatus.RESOLVED)
      throw new NotSerializableException(
        "tried to serialize a value with unresolved substitutions," +
          " need to Config#resolve() first, see API docs"
      )
    var field = new SerializedConfigValue.FieldOut(SerializedField.ROOT_VALUE)
    SerializedConfigValue.writeValue(field.data, value, null /* baseOrigin */ )
    SerializedConfigValue.writeField(out, field)
    field = new SerializedConfigValue.FieldOut(SerializedField.ROOT_WAS_CONFIG)
    field.data.writeBoolean(wasConfig)
    SerializedConfigValue.writeField(out, field)
    SerializedConfigValue.writeEndMarker(out)
  }
  @throws[IOException]
  @throws[ClassNotFoundException]
  override def readExternal(in: ObjectInput): Unit = {
    breakable {
      while (true) {
        val code = SerializedConfigValue.readCode(in)
        if (code eq SerializedField.END_MARKER)
          break() // break - was return
        val input = fieldIn(in)
        if (code eq SerializedField.ROOT_VALUE)
          this.value = SerializedConfigValue.readValue(input, null)
        else if (code eq SerializedField.ROOT_WAS_CONFIG)
          this.wasConfig = input.readBoolean
      }
    }
  }
  @throws[IOException]
  private def fieldIn(in: ObjectInput) = {
    val bytes = new Array[Byte](in.readInt)
    in.readFully(bytes)
    new DataInputStream(new ByteArrayInputStream(bytes))
  }
  override def valueType = throw SerializedConfigValue.shouldNotBeUsed
  override def unwrapped = throw SerializedConfigValue.shouldNotBeUsed
  override def newCopy(origin: ConfigOrigin) =
    throw SerializedConfigValue.shouldNotBeUsed
  override final def toString: String =
    getClass.getSimpleName + "(value=" + value + ",wasConfig=" + wasConfig + ")"
  override def equals(other: Any): Boolean = {
    // there's no reason we will ever call this equals(), but
    // the one in AbstractConfigValue would explode due to
    // calling unwrapped() above, so we just give some
    // safe-to-call implementation to avoid breaking the
    // contract of java.lang.Object
    if (other.isInstanceOf[SerializedConfigValue])
      canEqual(other) && (this.wasConfig == other
        .asInstanceOf[SerializedConfigValue]
        .wasConfig) &&
      (this.value == other.asInstanceOf[SerializedConfigValue].value)
    else false
  }
  override def hashCode: Int = {
    var h = 41 * (41 + value.hashCode)
    h = 41 * (h + (if (wasConfig) 1
                   else 0))
    h
  }
}
