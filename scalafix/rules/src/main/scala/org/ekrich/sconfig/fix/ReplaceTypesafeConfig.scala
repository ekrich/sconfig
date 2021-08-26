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
    doc.tree
      .collect {
        case tree @ ConfigEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigIncludeContextEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigValueEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigMemorySizeEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigObjectEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigOriginEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigParseOptionsEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigRenderOptionsEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigResolveOptionsEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigDocumentEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
        case tree @ ConfigNodeEmptyParenFunCall(term) =>
          Patch.replaceTree(tree, term.toString())
      }
      .fold(Patch.empty)(_ + _)
}

private object ConfigEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "entrySet",
    "root",
    "origin",
    "isEmpty",
    "isResolved"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/Config#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigIncludeContextEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "parseOptions"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigIncludeContext#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigValueEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "origin",
    "render",
    "unwrapped",
    "valueType"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigValue#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigMemorySizeEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "toBytes"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigMemorySize#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigObjectEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "toConfig"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigObject#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigOriginEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "comments",
    "description",
    "filename",
    "lineNumber",
    "resource",
    "url",
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigOrigin#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigParseOptionsEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "defaults",
    "getAllowMissing",
    "getClassLoader",
    "getIncluder",
    "getOriginDescription",
    "getSyntax"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigParseOptions#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigRenderOptionsEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "concise",
    "defaults",
    "getComments",
    "getFormatted",
    "getJson",
    "getOriginComments"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigRenderOptions#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigResolveOptionsEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "defaults",
    "getAllowUnresolved",
    "getResolver",
    "getUseSystemEnvironment",
    "noSystem"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/ConfigResolveOptions#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigDocumentEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "render"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/parser/ConfigDocument#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object ConfigNodeEmptyParenFunCall {
  private val emptyParenMethodNames = List(
    "render"
  )
  private val typesafeConfigSymbol = Symbol("com/typesafe/config/parser/ConfigNode#")

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(typesafeConfigSymbol, emptyParenMethodNames)(tree)
}

private object EmptyParenFunCallsOnSymbol {
  def apply(symbol: Symbol, methodNames: List[String])
           (tree: Tree)
           (implicit doc: SemanticDocument): Option[Term] = {

    object SelectSymbolEmptyParenMethod {
      def unapply(subtree: Tree)(implicit doc: SemanticDocument): Option[Term] =
        PartialFunction.condOpt(subtree) {
          case term @ Term.Select(_, SymbolEmptyParenMethod(_)) => term
        }
    }

    object DoesSymbolHaveCorrectType {
      def unapply(sym: Symbol)(implicit doc: Symtab): Boolean = {
        sym.isTypeOf(symbol)
      }
    }

    object SymbolEmptyParenMethod {
      def unapply(term: Term.Name)
                 (implicit doc: SemanticDocument): Option[MethodSignature] =
        PartialFunction.condOpt(term) {
          case Term.Name(name) & XSymbol(XSymbol.Owner(DoesSymbolHaveCorrectType()) & XSignature(sig: MethodSignature)) if methodNames contains name =>
            sig
        }
    }

    PartialFunction.condOpt(tree) {
      case Term.Apply(SelectSymbolEmptyParenMethod(term), List()) =>
        term
    }
  }

}
