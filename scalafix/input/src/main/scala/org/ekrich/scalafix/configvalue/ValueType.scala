/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configvalue

import com.typesafe.config.ConfigValue

trait ValueType {
  def configValue: ConfigValue
  def valueType(): Unit
}

object ValueType {
  def apply(a: ValueType): Unit = {
    a.configValue.valueType()

    a.valueType()
  }
}
