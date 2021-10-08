/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configresolveoptions

import com.typesafe.config.ConfigResolveOptions

trait GetAllowUnresolved {
  def configResolveOptions: ConfigResolveOptions
  def getAllowUnresolved(): Unit
}

object GetAllowUnresolved {
  def apply(a: GetAllowUnresolved): Unit = {
    a.configResolveOptions.getAllowUnresolved()

    a.getAllowUnresolved()
  }
}
