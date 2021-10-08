/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configorigin

import com.typesafe.config.ConfigOrigin

trait Comments {
  def configOrigin: ConfigOrigin
  def comments(): Unit
}

object Comments {
  def apply(a: Comments): Unit = {
    a.configOrigin.comments()

    a.comments()
  }
}
