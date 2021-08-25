package org.ekrich.example

import org.ekrich.config.Config

trait Foo {
  def conf: Config
  def entrySet(): Unit
}

object Foo {
  def apply(foo: Foo): Unit = {
    foo.conf.entrySet

    foo.entrySet()

    ()
  }
}
