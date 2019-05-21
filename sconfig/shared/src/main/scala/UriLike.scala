package org.ekrich.config.impl

import java.net.{URI, URL}

/**
 * To workaround missing implementation in Scala.js
 */
trait UriLike {
  def toURL(): URL
}
