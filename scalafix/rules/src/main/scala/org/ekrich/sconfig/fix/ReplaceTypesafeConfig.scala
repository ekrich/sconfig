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

private object ConfigEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/Config#"),
      "entrySet",
      "root",
      "origin",
      "isEmpty",
      "isResolved"
    )

private object ConfigIncludeContextEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigIncludeContext#"),
      "parseOptions"
    )

private object ConfigValueEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigValue#"),
      "origin",
      "render",
      "unwrapped",
      "valueType"
    )

private object ConfigMemorySizeEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigMemorySize#"),
      "toBytes"
    )

private object ConfigObjectEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigObject#"),
      "toConfig"
    )

private object ConfigOriginEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigOrigin#"),
      "comments",
      "description",
      "filename",
      "lineNumber",
      "resource",
      "url"
    )

private object ConfigParseOptionsEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigParseOptions#"),
      "defaults",
      "getAllowMissing",
      "getClassLoader",
      "getIncluder",
      "getOriginDescription",
      "getSyntax"
    )

private object ConfigRenderOptionsEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigRenderOptions#"),
      "concise",
      "defaults",
      "getComments",
      "getFormatted",
      "getJson",
      "getOriginComments",
      "getShowEnvVariableValues"
    )

private object ConfigResolveOptionsEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/ConfigResolveOptions#"),
      "defaults",
      "getAllowUnresolved",
      "getResolver",
      "getUseSystemEnvironment",
      "noSystem"
    )

private object ConfigDocumentEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/parser/ConfigDocument#"),
      "render"
    )

private object ConfigNodeEmptyParenFunCall
    extends AbstractEmptyParenFunCall(
      Symbol("com/typesafe/config/parser/ConfigNode#"),
      "render"
    )

private abstract class AbstractEmptyParenFunCall(
    typesafeConfigSymbol: Symbol,
    atLeastOneMethodName: String,
    emptyParenMethodNames: String*
) {
  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Term] =
    EmptyParenFunCallsOnSymbol(
      typesafeConfigSymbol,
      atLeastOneMethodName +: emptyParenMethodNames,
      tree
    )
}

private object EmptyParenFunCallsOnSymbol {
  def apply(symbol: Symbol, methodNames: Seq[String], tree: Tree)(implicit
      doc: SemanticDocument
  ): Option[Term] = {

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
      def unapply(
          term: Term.Name
      )(implicit doc: SemanticDocument): Option[MethodSignature] =
        PartialFunction.condOpt(term) {
          case Term.Name(name) & XSymbol(
                XSymbol.Owner(DoesSymbolHaveCorrectType()) & XSignature(
                  sig: MethodSignature
                )
              ) if methodNames contains name =>
            sig
        }
    }

    PartialFunction.condOpt(tree) {
      case Term.Apply.Initial(SelectSymbolEmptyParenMethod(term), List()) =>
        term
    }
  }
}
