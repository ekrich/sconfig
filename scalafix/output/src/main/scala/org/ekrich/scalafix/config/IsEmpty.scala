package org.ekrich.scalafix.config

import org.ekrich.config.Config

trait IsEmpty {
  def conf: Config
  def isEmpty(): Unit
}

object IsEmpty {
  def apply(a: IsEmpty): Unit = {
    a.conf.isEmpty

    a.isEmpty()
  }
}
