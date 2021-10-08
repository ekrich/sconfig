package org.ekrich.scalafix.configvalue

import org.ekrich.config.ConfigValue

trait Render {
  def configValue: ConfigValue
  def render(): Unit
}

object Render {
  def apply(a: Render): Unit = {
    a.configValue.render

    a.render()
  }
}
