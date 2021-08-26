/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configparseoptions

import com.typesafe.config.ConfigParseOptions

trait GetClassLoader {
  def configParseOptions: ConfigParseOptions
  def getClassLoader(): Unit
}

object GetClassLoader {
  def apply(a: GetClassLoader): Unit = {
    a.configParseOptions.getClassLoader()

    a.getClassLoader()
  }
}
