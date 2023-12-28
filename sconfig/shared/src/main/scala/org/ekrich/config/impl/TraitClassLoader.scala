package org.ekrich.config.impl

import java.net.URL
import java.{util => ju}

/**
 * To workaround missing implementations in Scala.js and Scala Native
 */
trait TraitClassLoader {
  def getResources(name: String): ju.Enumeration[URL]
}
