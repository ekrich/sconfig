/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.config

import com.typesafe.config.Config

trait Origin {
  def conf: Config
  def origin(): Unit
}

object Origin {
  def apply(a: Origin): Unit = {
    a.conf.origin()

    a.origin()
  }
}
