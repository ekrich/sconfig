/**
 *   Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{util => ju}

class ConfigNodeSingleToken(val token: Token) extends AbstractConfigNode {
  override def tokens: ju.Collection[Token] =
    ju.Collections.singletonList(token)
}
