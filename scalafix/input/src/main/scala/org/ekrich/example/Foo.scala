/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.example

import com.typesafe.config.Config

trait Foo {
  def conf: Config
  def entrySet(): Unit
}

object Foo {
  def apply(foo: Foo): Unit = {
    foo.conf.entrySet()

    foo.entrySet()

    ()
  }
}
