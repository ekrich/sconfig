package org.ekrich.scalafix.config

import org.ekrich.config.Config

trait Root {
  def conf: Config
  def root(): Unit
}

object Root {
  def apply(a: Root): Unit = {
    a.conf.root

    a.root()
  }
}
