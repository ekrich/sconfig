package org.ekrich.config.impl

import java.net.URL

/**
 * To workaround missing implementation in Scala.js
 */
trait UriLike {
  def toURL(): URL
}
