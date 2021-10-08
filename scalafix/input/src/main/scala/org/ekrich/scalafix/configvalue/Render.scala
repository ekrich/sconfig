/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configvalue

import com.typesafe.config.ConfigValue

trait Render {
  def configValue: ConfigValue
  def render(): Unit
}

object Render {
  def apply(a: Render): Unit = {
    a.configValue.render()

    a.render()
  }
}
