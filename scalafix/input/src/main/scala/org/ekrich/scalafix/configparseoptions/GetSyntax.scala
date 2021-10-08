/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configparseoptions

import com.typesafe.config.ConfigParseOptions

trait GetSyntax {
  def configParseOptions: ConfigParseOptions
  def getSyntax(): Unit
}

object GetSyntax {
  def apply(a: GetSyntax): Unit = {
    a.configParseOptions.getSyntax()

    a.getSyntax()
  }
}
