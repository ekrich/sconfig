package org.ekrich.config.impl

import java.{lang => jl}

import org.ekrich.config.ConfigValue
import org.ekrich.config.ConfigValueType
import org.ekrich.config.ConfigException

enum SerializedValueType(val configType: ConfigValueType) 
  extends jl.Enum[SerializedValueType] {
    // the ordinals here are in the wire format, caution
    case NULL extends SerializedValueType(ConfigValueType.NULL)
    case BOOLEAN extends SerializedValueType(ConfigValueType.BOOLEAN)
    case INT extends SerializedValueType(ConfigValueType.NUMBER)
    case LONG extends SerializedValueType(ConfigValueType.NUMBER)
    case DOUBLE extends SerializedValueType(ConfigValueType.NUMBER)
    case STRING extends SerializedValueType(ConfigValueType.STRING)
    case LIST extends SerializedValueType(ConfigValueType.LIST)
    case OBJECT extends SerializedValueType(ConfigValueType.OBJECT)
}

object SerializedValueType {
  private[impl] def forInt(b: Int): SerializedValueType =
    if (b < values.length) values(b)
    else null // really?

  private[impl] def forValue(value: ConfigValue): SerializedValueType = {
    val t = value.valueType
    if (t eq ConfigValueType.NUMBER) {
      if (value.isInstanceOf[ConfigInt]) return INT
      else if (value.isInstanceOf[ConfigLong]) return LONG
      else if (value.isInstanceOf[ConfigDouble]) return DOUBLE
    } else {
      for (st <- values) {
        if (st.configType eq t) return st
      }
    }
    throw new ConfigException.BugOrBroken(
      "don't know how to serialize " + value)
  }
}
