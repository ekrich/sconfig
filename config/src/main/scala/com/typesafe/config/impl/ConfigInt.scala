/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.typesafe.config.impl

import java.{lang => jl}
import java.io.ObjectStreamException
import java.io.Serializable
import com.typesafe.config.ConfigOrigin
import com.typesafe.config.ConfigValueType

@SerialVersionUID(2L)
final class ConfigInt(origin: ConfigOrigin,
                      val value: Int,
                      originalText: String)
    extends ConfigNumber(origin, originalText)
    with Serializable {

  override def valueType: ConfigValueType = ConfigValueType.NUMBER

  override def unwrapped: Integer = value

  override def transformToString: String = {
    val s = super.transformToString
    if (s == null) Integer.toString(value) else s
  }

  override def longValue: Long = value

  override def doubleValue: Double = value

  override def newCopy(origin: ConfigOrigin) =
    new ConfigInt(origin, value, originalText)

  // serialization all goes through SerializedConfigValue
  @throws[ObjectStreamException]
  private def writeReplace(): jl.Object = new SerializedConfigValue(this)
}
