/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configrenderoptions

import com.typesafe.config.ConfigRenderOptions

trait GetComments {
  def configRenderOptions: ConfigRenderOptions
  def getComments(): Unit
}

object GetComments {
  def apply(a: GetComments): Unit = {
    a.configRenderOptions.getComments()

    a.getComments()
  }
}
