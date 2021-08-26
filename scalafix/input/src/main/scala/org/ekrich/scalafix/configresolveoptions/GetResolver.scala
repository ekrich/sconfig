/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configresolveoptions

import com.typesafe.config.ConfigResolveOptions

trait GetResolver {
  def configResolveOptions: ConfigResolveOptions
  def getResolver(): Unit
}

object GetResolver {
  def apply(a: GetResolver): Unit = {
    a.configResolveOptions.getResolver()

    a.getResolver()
  }
}
