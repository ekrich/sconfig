package org.ekrich.config.impl

import java.{util => ju}
import scala.jdk.CollectionConverters._

final class ConfigNodeInclude(
    final val children: ju.Collection[AbstractConfigNode],
    private[impl] val kind: ConfigIncludeKind,
    private[impl] val isRequired: Boolean
) extends AbstractConfigNode {
  override def tokens: ju.Collection[Token] = {
    val tokens = new ju.ArrayList[Token]
    for (child <- children.asScala) {
      tokens.addAll(child.tokens)
    }
    tokens
  }

  private[impl] def name: String = {
    children.asScala.find(_.isInstanceOf[ConfigNodeSimpleValue]) match {
      case Some(node) =>
        Tokens
          .getValue(node.asInstanceOf[ConfigNodeSimpleValue].token)
          .unwrapped
          .asInstanceOf[String]
      case None => null
    }
  }
}
