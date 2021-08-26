/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configorigin

import com.typesafe.config.ConfigOrigin

trait Filename {
  def configOrigin: ConfigOrigin
  def filename(): Unit
}

object Filename {
  def apply(a: Filename): Unit = {
    a.configOrigin.filename()

    a.filename()
  }
}
