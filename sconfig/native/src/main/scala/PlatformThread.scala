package org.ekrich.config.impl

/**
 * To workaround missing implementations
 */
class PlatformThread(thread: Thread) extends ThreadLike {
  def getContextClassLoader(): ClassLoader = ???
}
