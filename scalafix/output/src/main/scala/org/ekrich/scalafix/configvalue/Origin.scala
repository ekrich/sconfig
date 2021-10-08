package org.ekrich.scalafix.configvalue

import org.ekrich.config.ConfigValue

trait Origin {
  def configValue: ConfigValue
  def origin(): Unit
}

object Origin {
  def apply(a: Origin): Unit = {
    a.configValue.origin

    a.origin()
  }
}
