package org.ekrich.sconfig.fix

import scalafix.v1._
import scala.meta._

class ReplaceTypesafeConfig extends SemanticRule("ReplaceTypesafeConfig") {
  override def fix(implicit doc: SemanticDocument): Patch =
    List(rewriteEntrySet, rewritePackages).fold(Patch.empty)(_ + _)

  def rewritePackages(implicit doc: SemanticDocument): Patch =
    Patch.replaceSymbols(
      "com.typesafe.config" -> "org.ekrich.config"
    )

  def rewriteEntrySet(implicit doc: SemanticDocument): Patch =
    doc.tree.collect {
      case tree@ConfigEntrySetFunCall(term) =>
        Patch.replaceTree(tree, term.toString())
    }.fold(Patch.empty)(_ + _)
}

private object ConfigEntrySetFunCall {
  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    PartialFunction.condOpt(tree) {
      // config.entrySet()
      case Term.Apply(ConfigEntrySet(term), List()) =>
        term
    }
}

private object ConfigEntrySet {
  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    PartialFunction.condOpt(tree) {
      case term@Term.Select(_, ConfigEntrySetMethod(_)) => term
    }
}

private object ConfigEntrySetMethod {
  def unapply(term: Term.Name)(implicit doc: SemanticDocument): Option[MethodSignature] =
    PartialFunction.condOpt(term) {
      case Term.Name("entrySet") & XSymbol(XSymbol.Owner(IsSymbolATypeOfTypesafeConfig()) & XSignature(sig: MethodSignature)) => sig
    }
}

private object IsSymbolATypeOfTypesafeConfig {
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/Config#")

  def unapply(sym: Symbol)(implicit doc: Symtab): Boolean = {
    sym.isTypeOf(typesafeConfigSymbol)
  }
}
