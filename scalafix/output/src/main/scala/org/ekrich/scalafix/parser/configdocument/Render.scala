package org.ekrich.scalafix.parser.configdocument

import org.ekrich.config.parser.ConfigDocument

trait Render {
  def configDocument: ConfigDocument
  def render(): Unit
}

object Render {
  def apply(a: Render): Unit = {
    a.configDocument.render

    a.render()
  }
}
