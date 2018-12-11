package com.typesafe.config.impl

import java.{util => ju}

final class ConfigNodeInclude(
    final val children: ju.Collection[AbstractConfigNode],
    private[impl] val kind: ConfigIncludeKind,
    private[impl] val isRequired: Boolean)
    extends AbstractConfigNode {

  override def tokens: ju.Collection[Token] = {
    val tokens = new ju.ArrayList[Token]
    import scala.collection.JavaConverters._
    for (child <- children.asScala) {
      tokens.addAll(child.tokens)
    }
    tokens
  }

  private[impl] def name: String = {
    import scala.collection.JavaConverters._
    for (n <- children.asScala) {
      if (n.isInstanceOf[ConfigNodeSimpleValue])
        return Tokens
          .getValue(n.asInstanceOf[ConfigNodeSimpleValue].token)
          .unwrapped
          .asInstanceOf[String]
    }
    null
  }
}
