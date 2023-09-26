package org.ekrich.config.impl

import java.net.URL
import java.{util => ju}

/**
 * To workaround missing implementations
 */
class PlatformClassLoader(cl: ClassLoader) extends ClassLoaderLike {
  def getResources(name: String): ju.Enumeration[URL] = cl.getResources(name)
}
