/**
 * Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{util => ju}
import org.ekrich.config.ConfigOrigin

abstract class ConfigNodeComplexValue(
    _children: ju.Collection[AbstractConfigNode],
    // null for nodes built without one; ConfigParser falls back to its line
    // counter then. ConfigNodeRoot inherits this rather than declaring its
    // own -- Scala has no field shadowing.
    val origin: ConfigOrigin = null
) extends AbstractConfigNodeValue {
  // why create ArrayList from Collection?
  val children: ju.List[AbstractConfigNode] =
    new ju.ArrayList[AbstractConfigNode](_children)

  override def tokens: ju.Collection[Token] = {
    val tokens = new ju.ArrayList[Token]
    children.forEach { child =>
      tokens.addAll(child.tokens)
    }
    tokens
  }
  private[impl] def indentText(
      indentation: AbstractConfigNode
  ): ConfigNodeComplexValue = {
    val childrenCopy =
      new ju.ArrayList[AbstractConfigNode](children)
    var i = 0
    while (i < childrenCopy.size) {
      val child = childrenCopy.get(i)
      if (child.isInstanceOf[ConfigNodeSingleToken] && Tokens.isNewline(
            child.asInstanceOf[ConfigNodeSingleToken].token
          )) {
        childrenCopy.add(i + 1, indentation)
        i += 1
      } else if (child.isInstanceOf[ConfigNodeField]) {
        val value =
          child.asInstanceOf[ConfigNodeField].value
        if (value.isInstanceOf[ConfigNodeComplexValue])
          childrenCopy.set(
            i,
            child
              .asInstanceOf[ConfigNodeField]
              .replaceValue(
                value
                  .asInstanceOf[ConfigNodeComplexValue]
                  .indentText(indentation)
              )
          )
      } else if (child.isInstanceOf[ConfigNodeComplexValue])
        childrenCopy.set(
          i,
          child
            .asInstanceOf[ConfigNodeComplexValue]
            .indentText(indentation)
        )

      i += 1
    }
    newNode(childrenCopy)
  }
  // This method will just call into the object's constructor, but it's needed
  // for use in the indentText() method so we can avoid a gross if/else statement
  // checking the type of this
  def newNode(nodes: ju.Collection[AbstractConfigNode]): ConfigNodeComplexValue
}
