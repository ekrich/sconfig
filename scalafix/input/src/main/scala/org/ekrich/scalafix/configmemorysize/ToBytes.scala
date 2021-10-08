/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configmemorysize

import com.typesafe.config.ConfigMemorySize

trait ToBytes {
  def configMemorySize: ConfigMemorySize
  def toBytes(): Unit
}

object ToBytes {
  def apply(a: ToBytes): Unit = {
    a.configMemorySize.toBytes()

    a.toBytes()
  }
}
