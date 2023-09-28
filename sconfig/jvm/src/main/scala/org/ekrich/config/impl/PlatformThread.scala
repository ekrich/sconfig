package org.ekrich.config.impl

/**
 * To workaround missing implementations in Scala.js and Scala Native
 */
class PlatformThread(thread: Thread) extends TraitThread {
  def getContextClassLoader(): ClassLoader = thread.getContextClassLoader()
}
