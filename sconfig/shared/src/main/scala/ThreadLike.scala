package org.ekrich.config.impl

/**
 * To workaround missing implementations in Scala.js and Scala Native
 */
trait ThreadLike {
  def getContextClassLoader(): ClassLoader
}
