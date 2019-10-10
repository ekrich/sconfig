package org.ekrich.config.impl

import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigSyntax
import java.{util => ju}

final class ConfigNodeRoot private[impl] (
    _children: ju.Collection[AbstractConfigNode],
    val origin: ConfigOrigin
) extends ConfigNodeComplexValue(_children) {

  override def newNode(nodes: ju.Collection[AbstractConfigNode]) =
    throw new ConfigException.BugOrBroken("Tried to indent the root object")

  private[impl] def value: ConfigNodeComplexValue = {
    import scala.jdk.CollectionConverters._
    for (node <- children.asScala) {
      if (node.isInstanceOf[ConfigNodeComplexValue])
        return node.asInstanceOf[ConfigNodeComplexValue]
    }
    throw new ConfigException.BugOrBroken(
      "ConfigNodeRoot did not contain a value"
    )
  }
  private[impl] def setValue(
      desiredPath: String,
      value: AbstractConfigNodeValue,
      flavor: ConfigSyntax
  ): ConfigNodeRoot = {
    val childrenCopy =
      new ju.ArrayList[AbstractConfigNode](children)
    var i = 0
    while (i < childrenCopy.size) {
      val node = childrenCopy.get(i)
      if (node.isInstanceOf[ConfigNodeComplexValue])
        if (node.isInstanceOf[ConfigNodeArray])
          throw new ConfigException.WrongType(
            origin,
            "The ConfigDocument had an array at the root level, and values cannot be modified inside an array."
          )
        else if (node.isInstanceOf[ConfigNodeObject]) {
          if (value == null)
            childrenCopy.set(
              i,
              node
                .asInstanceOf[ConfigNodeObject]
                .removeValueOnPath(desiredPath, flavor)
            )
          else
            childrenCopy.set(
              i,
              node
                .asInstanceOf[ConfigNodeObject]
                .setValueOnPath(desiredPath, value, flavor)
            )
          return new ConfigNodeRoot(childrenCopy, origin)
        }
      i += 1
    }
    throw new ConfigException.BugOrBroken(
      "ConfigNodeRoot did not contain a value"
    )
  }
  private[impl] def hasValue(desiredPath: String): Boolean = {
    val path = PathParser.parsePath(desiredPath)
    val childrenCopy =
      new ju.ArrayList[AbstractConfigNode](children)
    var i = 0
    while (i < childrenCopy.size) {
      val node = childrenCopy.get(i)
      if (node.isInstanceOf[ConfigNodeComplexValue])
        if (node.isInstanceOf[ConfigNodeArray])
          throw new ConfigException.WrongType(
            origin,
            "The ConfigDocument had an array at the root level, and values cannot be modified inside an array."
          )
        else if (node.isInstanceOf[ConfigNodeObject])
          return node.asInstanceOf[ConfigNodeObject].hasValue(path)

      i += 1
    }
    throw new ConfigException.BugOrBroken(
      "ConfigNodeRoot did not contain a value"
    )
  }
}
