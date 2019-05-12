package org.ekrich.config.impl

/**
 * To workaround missing implementation in Scala.js
 */
class PlatformThread(thread: Thread) extends ThreadLike {
  def getContextClassLoader(): ClassLoader = thread.getContextClassLoader()
}
