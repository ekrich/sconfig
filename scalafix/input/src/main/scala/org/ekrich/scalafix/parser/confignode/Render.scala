/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.parser.confignode

import com.typesafe.config.parser.ConfigNode

trait Render {
  def configNode: ConfigNode
  def render(): Unit
}

object Render {
  def apply(a: Render): Unit = {
    a.configNode.render()

    a.render()
  }
}
