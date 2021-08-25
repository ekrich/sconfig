package org.ekrich.sconfig.scalafix

import scalafix.v1._

class ReplaceTypesafeConfig extends SemanticRule("ReplaceTypesafeConfig") {
  override def fix(implicit doc: SemanticDocument): Patch =
    rewritePackages

  def rewritePackages(implicit doc: SemanticDocument): Patch =
    Patch.replaceSymbols(
      "com.typesafe.config" -> "org.ekrich.config"
    )
}
