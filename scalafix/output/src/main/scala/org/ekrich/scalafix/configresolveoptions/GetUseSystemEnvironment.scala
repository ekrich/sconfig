package org.ekrich.scalafix.configresolveoptions

import org.ekrich.config.ConfigResolveOptions

trait GetUseSystemEnvironment {
  def configResolveOptions: ConfigResolveOptions
  def getUseSystemEnvironment(): Unit
}

object GetUseSystemEnvironment {
  def apply(a: GetUseSystemEnvironment): Unit = {
    a.configResolveOptions.getUseSystemEnvironment

    a.getUseSystemEnvironment()
  }
}
