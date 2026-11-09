package org.ekrich.config.impl

/**
 * To workaround missing implementations
 */
class PlatformThread(thread: Thread) extends TraitThread {
  def getContextClassLoader(): ClassLoader = PlatformThread.emptyClassLoader
}

private object PlatformThread {
  // Scala.js has no class loader. The placeholder keeps the classpath fallback
  // of include resolution working: it finds no resources, as on a platform
  // without a classpath.
  val emptyClassLoader: ClassLoader = new ClassLoader {}
}
