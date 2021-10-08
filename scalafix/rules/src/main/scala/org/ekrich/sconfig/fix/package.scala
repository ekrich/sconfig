/*
 * Copyright 2018 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ekrich.sconfig

import scalafix.v1._

import scala.meta._
import scala.util.Properties
import scala.util.control.NonFatal

/*
 * Originally copied from
 * https://github.com/http4s/http4s/blob/9a19b6e201a6a0751517fd8b763078323c75dd66/scalafix/rules/src/main/scala/fix/package.scala
 */
package object fix {

  /** Allows to apply multiple extractors/matchers to a single expression in a `case` block.
   */
  object & {
    def unapply[A](a: A): Option[(A, A)] = Some((a, a))
  }

  object XTerm {
    // Matches if the term has curly braces.
    object IsBlockOrPartFunc {
      def unapply(term: Term): Boolean =
        PartialFunction.cond(term) {
          case Term.Block(_) | Term.PartialFunction(_) =>
            true
        }
    }
  }

  object XSymbol {
    def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Symbol] =
      Some(tree.symbol)

    object Owner {
      def unapply(sym: Symbol): Option[Symbol] = Some(sym.owner)
    }
  }

  implicit final class XSymbolOps(val self: Symbol) extends AnyVal {
    def isTypeOf(that: Symbol)(implicit doc: Symtab): Boolean =
      PartialFunction.cond(self) {
        case `that` => true
        case XSignature(ClassSignature(_, parents, _, _)) =>
          parents.iterator
            .collect { case TypeRef(_, parentSym, _) => parentSym }
            .exists(_.isTypeOf(that))
      }
  }

  object XSignature {
    def unapply(symInfo: SymbolInformation): Option[Signature] =
      Some(symInfo.signature)
    def unapply(sym: Symbol)(implicit doc: Symtab): Option[Signature] =
      try {
        sym.info.flatMap(unapply)
      } catch {
        case _: IllegalArgumentException
            if sym.value.startsWith("java")
              && Properties.isJavaAtLeast("10")
              && Properties.versionString.startsWith("version 2.11") =>
          /*
           * When we're on Scala 2.11 and Java versions greater than 9,
           * the scala-asm method ultimately called by `sym.info` above
           * throws an IllegalArgumentException because it doesn't support
           * inspecting class versions > 9. We don't need to return a
           * value when the symbol is java.* anyway, so work around the
           * limitation by ignoring the exception and returning None.
           *
           * This can be removed when support for Scala 2.11 is dropped.
           */
          None
        case NonFatal(ex) =>
          throw UnexpectedException(sym,
                                    Properties.javaVersion,
                                    Properties.versionString,
                                    ex)
      }
  }

  object XSemanticType {

    /** Tries to infer a result SemanticType of a particular Stat.
     *
     * @note Not all possible cases are handled.
     */
    def unapply(stat: Stat)(
        implicit doc: SemanticDocument): Option[SemanticType] =
      PartialFunction.condOpt(stat) {
        case Term.Name(_) & XSymbol(XSignature(ValueSignature(tpe))) => tpe

        case Term.Apply(_, _) &
              XSymbol(XSignature(ValueSignature(TypeRef(_, funcSym, _ :+ tpe))))
            if funcSym.value.startsWith("scala/Function") && funcSym.value
              .endsWith("#") =>
          tpe

        case Term.Block(_ :+ XSemanticType(tpe)) => tpe
      }
  }
}

case class UnexpectedException(sym: Symbol,
                               java: String,
                               scala: String,
                               cause: Throwable)
    extends RuntimeException(
      s"""An unexpected exception occurred. Please report this as an issue at https://github.com/ekrich/sconfig/issues and include the following information:
         |
         |Failing symbol: ${sym.value}
         |Java version:   $java
         |Scala version:  $scala
         |""".stripMargin,
      cause
    )
