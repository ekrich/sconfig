package org.ekrich.scalafix.configobject

import org.ekrich.config.ConfigObject

trait ToConfig {
  def configObject: ConfigObject
  def toConfig(): Unit
}

object ToConfig {
  def apply(a: ToConfig): Unit = {
    a.configObject.toConfig

    a.toConfig()
  }
}
