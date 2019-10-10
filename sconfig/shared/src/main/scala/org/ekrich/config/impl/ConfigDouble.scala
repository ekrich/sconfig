/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{lang => jl}
import java.io.ObjectStreamException
import java.io.Serializable
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigValueType

@SerialVersionUID(2L)
final class ConfigDouble(
    origin: ConfigOrigin,
    val value: jl.Double,
    originalText: String
) extends ConfigNumber(origin, originalText)
    with Serializable {

  override def valueType: ConfigValueType = ConfigValueType.NUMBER

  override def unwrapped: jl.Double = value

  override def transformToString: String = {
    val s = super.transformToString
    if (s == null) jl.Double.toString(value) else s
  }

  override def longValue: Long = value.toLong

  override def doubleValue: Double = value

  override def newCopy(origin: ConfigOrigin) =
    new ConfigDouble(origin, value, originalText)

  // serialization all goes through SerializedConfigValue
  @throws[ObjectStreamException]
  private def writeReplace(): jl.Object = new SerializedConfigValue(this)
}
