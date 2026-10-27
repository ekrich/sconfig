package org.ekrich.config.impl

import java.net.URL
import java.{util => ju}

/**
 * To workaround missing implementations
 *
 * Scala.js has no classpath, so there are never any resources; an empty
 * enumeration lets include resolution treat a missing classpath include the
 * same way as a missing file.
 */
class PlatformClassLoader(cl: ClassLoader) extends TraitClassLoader {
  def getResources(name: String): ju.Enumeration[URL] =
    new ju.Enumeration[URL] {
      def hasMoreElements: Boolean = false
      def nextElement(): URL =
        throw new ju.NoSuchElementException("no classpath resources: " + name)
    }
}
