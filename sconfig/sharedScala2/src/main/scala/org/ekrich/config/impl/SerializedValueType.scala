package org.ekrich.config.impl

import java.{lang => jl}

import org.ekrich.config.ConfigValue
import org.ekrich.config.ConfigValueType
import org.ekrich.config.ConfigException

final class SerializedValueType private (
    name: String,
    ordinal: Int,
    val configType: ConfigValueType
) extends jl.Enum[SerializedValueType](name, ordinal)

object SerializedValueType {
  // the ordinals here are in the wire format, caution
  final val NULL = new SerializedValueType("NULL", 0, ConfigValueType.NULL)
  final val BOOLEAN =
    new SerializedValueType("BOOLEAN", 1, ConfigValueType.BOOLEAN)
  final val INT  = new SerializedValueType("INT", 2, ConfigValueType.NUMBER)
  final val LONG = new SerializedValueType("LONG", 3, ConfigValueType.NUMBER)
  final val DOUBLE =
    new SerializedValueType("DOUBLE", 4, ConfigValueType.NUMBER)
  final val STRING =
    new SerializedValueType("STRING", 5, ConfigValueType.STRING)
  final val LIST = new SerializedValueType("LIST", 6, ConfigValueType.LIST)
  final val OBJECT =
    new SerializedValueType("OBJECT", 7, ConfigValueType.OBJECT)

  private[this] final val _values: Array[SerializedValueType] =
    Array(NULL, BOOLEAN, INT, LONG, DOUBLE, STRING, LIST, OBJECT)

  def values: Array[SerializedValueType] = _values.clone()

  def valueOf(name: String): SerializedValueType = {
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException(
        s"No enum const SerializedValueType.$name"
      )
    }
  }

  private[impl] def forInt(b: Int): SerializedValueType =
    if (b >= 0 && b < values.length)
      values(b)
    else
      throw new IllegalArgumentException(
        s"No enum SerializedValueType ordinal $b")

  private[impl] def forValue(value: ConfigValue): SerializedValueType = {
    val t = value.valueType
    if (t eq ConfigValueType.NUMBER) {
      if (value.isInstanceOf[ConfigInt]) return INT
      else if (value.isInstanceOf[ConfigLong]) return LONG
      else if (value.isInstanceOf[ConfigDouble]) return DOUBLE
    } else {
      var n = 0
      while (n < values.length) {
        val st = values(n)
        if (st.configType eq t) {
          return st
        }
        n += 1
      }
    }
    throw new ConfigException.BugOrBroken(
      "don't know how to serialize " + value
    )
  }
}
