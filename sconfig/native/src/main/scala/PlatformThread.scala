package org.ekrich.config.impl

/**
 * To workaround missing implementation
 */
class PlatformThread(thread: Thread) extends ThreadLike {
  def getContextClassLoader(): ClassLoader = ???
}
