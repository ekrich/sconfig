package org.ekrich.scalafix.configresolveoptions

import org.ekrich.config.ConfigResolveOptions

trait GetAllowUnresolved {
  def configResolveOptions: ConfigResolveOptions
  def getAllowUnresolved(): Unit
}

object GetAllowUnresolved {
  def apply(a: GetAllowUnresolved): Unit = {
    a.configResolveOptions.getAllowUnresolved

    a.getAllowUnresolved()
  }
}
