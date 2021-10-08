package org.ekrich.scalafix.configparseoptions

import org.ekrich.config.ConfigParseOptions

trait GetAllowMissing {
  def configParseOptions: ConfigParseOptions
  def getAllowMissing(): Unit
}

object GetAllowMissing {
  def apply(a: GetAllowMissing): Unit = {
    a.configParseOptions.getAllowMissing

    a.getAllowMissing()
  }
}
