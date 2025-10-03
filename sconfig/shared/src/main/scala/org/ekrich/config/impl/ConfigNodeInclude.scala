package org.ekrich.config.impl

import java.{util => ju}
import ScalaOps._

final class ConfigNodeInclude(
    final val children: ju.Collection[AbstractConfigNode],
    private[impl] val kind: ConfigIncludeKind,
    private[impl] val isRequired: Boolean
) extends AbstractConfigNode {
  override def tokens: ju.Collection[Token] = {
    val tokens = new ju.ArrayList[Token]
    children.forEach { child =>
      tokens.addAll(child.tokens)
    }
    tokens
  }

  private[impl] def name: String =
    children.scalaOps.findFold(_.isInstanceOf[ConfigNodeSimpleValue])(() =>
      null: String
    )(node =>
      Tokens
        .getValue(node.asInstanceOf[ConfigNodeSimpleValue].token)
        .unwrapped
        .asInstanceOf[String]
    )
}
