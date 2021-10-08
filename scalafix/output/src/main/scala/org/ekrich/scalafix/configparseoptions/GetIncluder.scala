package org.ekrich.scalafix.configparseoptions

import org.ekrich.config.ConfigParseOptions

trait GetIncluder {
  def configParseOptions: ConfigParseOptions
  def getIncluder(): Unit
}

object GetIncluder {
  def apply(a: GetIncluder): Unit = {
    a.configParseOptions.getIncluder

    a.getIncluder()
  }
}
