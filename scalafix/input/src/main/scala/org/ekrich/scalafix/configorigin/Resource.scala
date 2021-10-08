/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configorigin

import com.typesafe.config.ConfigOrigin

trait Resource {
  def configOrigin: ConfigOrigin
  def resource(): Unit
}

object Resource {
  def apply(a: Resource): Unit = {
    a.configOrigin.resource()

    a.resource()
  }
}
