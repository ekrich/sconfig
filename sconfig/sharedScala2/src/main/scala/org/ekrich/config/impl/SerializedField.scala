package org.ekrich.config.impl

import java.{lang => jl}

// this is how we try to be extensible
final class SerializedField private (name: String, ordinal: Int)
    extends jl.Enum[SerializedField](name, ordinal)

object SerializedField {
  // represents a field code we didn't recognize
  final val UNKNOWN = new SerializedField("UNKNOWN", 0)
  // end of a list of fields
  final val END_MARKER = new SerializedField("END_MARKER", 1)
  // Fields at the root
  final val ROOT_VALUE      = new SerializedField("ROOT_VALUE", 2)
  final val ROOT_WAS_CONFIG = new SerializedField("ROOT_WAS_CONFIG", 3)
  // Fields that make up a value
  final val VALUE_DATA   = new SerializedField("VALUE_DATA", 4)
  final val VALUE_ORIGIN = new SerializedField("VALUE_ORIGIN", 5)
  // Fields that make up an origin
  final val ORIGIN_DESCRIPTION = new SerializedField("ORIGIN_DESCRIPTION", 6)
  final val ORIGIN_LINE_NUMBER = new SerializedField("ORIGIN_LINE_NUMBER", 7)
  final val ORIGIN_END_LINE_NUMBER =
    new SerializedField("ORIGIN_END_LINE_NUMBER", 8)
  final val ORIGIN_TYPE     = new SerializedField("ORIGIN_TYPE", 9)
  final val ORIGIN_URL      = new SerializedField("ORIGIN_URL", 10)
  final val ORIGIN_COMMENTS = new SerializedField("ORIGIN_COMMENTS", 11)
  final val ORIGIN_NULL_URL = new SerializedField("ORIGIN_NULL_URL", 12)
  final val ORIGIN_NULL_COMMENTS =
    new SerializedField("ORIGIN_NULL_COMMENTS", 13)
  final val ORIGIN_RESOURCE = new SerializedField("ORIGIN_RESOURCE", 14)
  final val ORIGIN_NULL_RESOURCE =
    new SerializedField("ORIGIN_NULL_RESOURCE", 15)

  private[this] final val _values: Array[SerializedField] =
    Array(
      UNKNOWN,
      END_MARKER,
      ROOT_VALUE,
      ROOT_WAS_CONFIG,
      VALUE_DATA,
      VALUE_ORIGIN,
      ORIGIN_DESCRIPTION,
      ORIGIN_LINE_NUMBER,
      ORIGIN_END_LINE_NUMBER,
      ORIGIN_TYPE,
      ORIGIN_URL,
      ORIGIN_COMMENTS,
      ORIGIN_NULL_URL,
      ORIGIN_NULL_COMMENTS,
      ORIGIN_RESOURCE,
      ORIGIN_NULL_RESOURCE
    )

  def values: Array[SerializedField] = _values.clone()

  def valueOf(name: String): SerializedField = {
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum const SerializedField." + name)
    }
  }

  private[impl] def forInt(b: Int): SerializedField =
    if (b < values.length) values(b)
    else UNKNOWN
}
