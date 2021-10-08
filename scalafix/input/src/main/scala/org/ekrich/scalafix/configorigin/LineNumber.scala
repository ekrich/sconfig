/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configorigin

import com.typesafe.config.ConfigOrigin

trait LineNumber {
  def configOrigin: ConfigOrigin
  def lineNumber(): Unit
}

object LineNumber {
  def apply(a: LineNumber): Unit = {
    a.configOrigin.lineNumber()

    a.lineNumber()
  }
}
