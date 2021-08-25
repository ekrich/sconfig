package org.ekrich.example

import org.ekrich.config.Config

trait Foo {
  def conf: Config
}

object Foo {
  def apply(uc: Foo): Unit = {
    uc.conf.entrySet

    ()
  }
}
