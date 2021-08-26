package org.ekrich.scalafix.configresolveoptions

import org.ekrich.config.ConfigResolveOptions

trait GetResolver {
  def configResolveOptions: ConfigResolveOptions
  def getResolver(): Unit
}

object GetResolver {
  def apply(a: GetResolver): Unit = {
    a.configResolveOptions.getResolver

    a.getResolver()
  }
}
