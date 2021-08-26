/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.config

import com.typesafe.config.Config

trait Root {
  def conf: Config
  def root(): Unit
}

object Root {
  def apply(a: Root): Unit = {
    a.conf.root()

    a.root()
  }
}
