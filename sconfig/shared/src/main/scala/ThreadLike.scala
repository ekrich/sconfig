package org.ekrich.config.impl

/**
 * To workaround missing implementation in Scala.js
 */
trait ThreadLike {
  def getContextClassLoader(): ClassLoader
}
