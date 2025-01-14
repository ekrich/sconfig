package org.ekrich.config.impl

import java.{util => ju}

final class ConfigNodeConcatenation private[impl] (
    children: ju.Collection[AbstractConfigNode]
) extends ConfigNodeComplexValue(children) {
  override def newNode(
      nodes: ju.Collection[AbstractConfigNode]
  ): ConfigNodeComplexValue =
    new ConfigNodeConcatenation(nodes)
}
