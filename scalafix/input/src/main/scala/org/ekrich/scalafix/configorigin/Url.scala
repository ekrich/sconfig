/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configorigin

import com.typesafe.config.ConfigOrigin

trait Url {
  def configOrigin: ConfigOrigin
  def url(): Unit
}

object Url {
  def apply(a: Url): Unit = {
    a.configOrigin.url()

    a.url()
  }
}
