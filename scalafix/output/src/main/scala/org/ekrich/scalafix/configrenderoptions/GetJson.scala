package org.ekrich.scalafix.configrenderoptions

import org.ekrich.config.ConfigRenderOptions

trait GetJson {
  def configRenderOptions: ConfigRenderOptions
  def getJson(): Unit
}

object GetJson {
  def apply(a: GetJson): Unit = {
    a.configRenderOptions.getJson

    a.getJson()
  }
}
