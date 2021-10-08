/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.config

import com.typesafe.config.Config

trait IsResolved {
  def conf: Config
  def isResolved(): Unit
}

object IsResolved {
  def apply(a: IsResolved): Unit = {
    a.conf.isResolved()

    a.isResolved()
  }
}
