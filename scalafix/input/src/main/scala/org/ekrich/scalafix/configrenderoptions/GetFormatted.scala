/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configrenderoptions

import com.typesafe.config.ConfigRenderOptions

trait GetFormatted {
  def configRenderOptions: ConfigRenderOptions
  def getFormatted(): Unit
}

object GetFormatted {
  def apply(a: GetFormatted): Unit = {
    a.configRenderOptions.getFormatted()

    a.getFormatted()
  }
}
