package org.ekrich.scalafix.config

import org.ekrich.config.Config

trait Origin {
  def conf: Config
  def origin(): Unit
}

object Origin {
  def apply(a: Origin): Unit = {
    a.conf.origin

    a.origin()
  }
}
