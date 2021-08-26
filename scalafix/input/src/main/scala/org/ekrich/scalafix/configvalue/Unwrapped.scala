/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configvalue

import com.typesafe.config.ConfigValue

trait Unwrapped {
  def configValue: ConfigValue
  def unwrapped(): Unit
}

object Unwrapped {
  def apply(a: Unwrapped): Unit = {
    a.configValue.unwrapped()

    a.unwrapped()
  }
}
