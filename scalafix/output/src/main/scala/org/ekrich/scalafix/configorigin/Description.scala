package org.ekrich.scalafix.configorigin

import org.ekrich.config.ConfigOrigin

trait Description {
  def configOrigin: ConfigOrigin
  def description(): Unit
}

object Description {
  def apply(a: Description): Unit = {
    a.configOrigin.description

    a.description()
  }
}
