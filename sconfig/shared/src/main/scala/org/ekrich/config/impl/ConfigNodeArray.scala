package org.ekrich.config.impl

import java.{util => ju}
import org.ekrich.config.ConfigOrigin

final class ConfigNodeArray private[impl] (
    _children: ju.Collection[AbstractConfigNode],
    _origin: ConfigOrigin = null
) extends ConfigNodeComplexValue(_children, _origin) {
  override def newNode(
      nodes: ju.Collection[AbstractConfigNode]
  ): ConfigNodeComplexValue =
    new ConfigNodeArray(nodes, origin)
}
