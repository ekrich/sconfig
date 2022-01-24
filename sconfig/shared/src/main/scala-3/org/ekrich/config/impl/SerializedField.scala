package org.ekrich.config.impl

import java.{lang => jl}

// this is how we try to be extensible
enum SerializedField extends jl.Enum[SerializedField] {
  case UNKNOWN, // represents a field code we didn't recognize
    END_MARKER, // end of a list of fields
    ROOT_VALUE, // Fields at the root
    ROOT_WAS_CONFIG,
    VALUE_DATA, // Fields that make up a value
    VALUE_ORIGIN,
    ORIGIN_DESCRIPTION, // Fields that make up an origin
    ORIGIN_LINE_NUMBER,
    ORIGIN_END_LINE_NUMBER,
    ORIGIN_TYPE,
    ORIGIN_URL,
    ORIGIN_COMMENTS,
    ORIGIN_NULL_URL,
    ORIGIN_NULL_COMMENTS,
    ORIGIN_RESOURCE,
    ORIGIN_NULL_RESOURCE
}
object SerializedField {
  private[impl] def forInt(b: Int): SerializedField =
    if (b >= 0 && b < values.length) values(b)
    else UNKNOWN
}
