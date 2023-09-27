package org.ekrich.config.impl

import java.net.{URI, URL}

/**
 * To workaround missing implementations
 */
class PlatformUri(uri: URI) extends UriLike {
  def toURL(): URL = uri.toURL()
}
