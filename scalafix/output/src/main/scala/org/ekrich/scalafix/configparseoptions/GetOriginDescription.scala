package org.ekrich.scalafix.configparseoptions

import org.ekrich.config.ConfigParseOptions

trait GetOriginDescription {
  def configParseOptions: ConfigParseOptions
  def getOriginDescription(): Unit
}

object GetOriginDescription {
  def apply(a: GetOriginDescription): Unit = {
    a.configParseOptions.getOriginDescription

    a.getOriginDescription()
  }
}
