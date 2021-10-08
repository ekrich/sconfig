package org.ekrich.scalafix.configrenderoptions

import org.ekrich.config.ConfigRenderOptions

trait GetOriginComments {
  def configRenderOptions: ConfigRenderOptions
  def getOriginComments(): Unit
}

object GetOriginComments {
  def apply(a: GetOriginComments): Unit = {
    a.configRenderOptions.getOriginComments

    a.getOriginComments()
  }
}
