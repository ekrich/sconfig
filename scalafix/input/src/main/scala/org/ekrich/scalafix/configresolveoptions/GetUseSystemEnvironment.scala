/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configresolveoptions

import com.typesafe.config.ConfigResolveOptions

trait GetUseSystemEnvironment {
  def configResolveOptions: ConfigResolveOptions
  def getUseSystemEnvironment(): Unit
}

object GetUseSystemEnvironment {
  def apply(a: GetUseSystemEnvironment): Unit = {
    a.configResolveOptions.getUseSystemEnvironment()

    a.getUseSystemEnvironment()
  }
}
