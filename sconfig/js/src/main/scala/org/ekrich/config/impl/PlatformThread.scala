package org.ekrich.config.impl

/**
 * To workaround missing implementations
 */
class PlatformThread(thread: Thread) extends TraitThread {
  def getContextClassLoader(): ClassLoader = ???
}
