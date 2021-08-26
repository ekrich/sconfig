package org.ekrich.scalafix.config

import org.ekrich.config.Config

trait EntrySet {
  def conf: Config
  def entrySet(): Unit
}

object EntrySet {
  def apply(a: EntrySet): Unit = {
    a.conf.entrySet

    a.entrySet()
  }
}
