package org.ekrich.scalafix.configorigin

import org.ekrich.config.ConfigOrigin

trait Resource {
  def configOrigin: ConfigOrigin
  def resource(): Unit
}

object Resource {
  def apply(a: Resource): Unit = {
    a.configOrigin.resource

    a.resource()
  }
}
