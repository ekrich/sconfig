/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configincludecontext

import com.typesafe.config.ConfigIncludeContext

trait ParseOptions {
  def configIncludeContext: ConfigIncludeContext
  def parseOptions(): Unit
}

object ParseOptions {
  def apply(a: ParseOptions): Unit = {
    a.configIncludeContext.parseOptions()

    a.parseOptions()
  }
}
