package org.ekrich.config.impl

/**
 * To workaround missing implementations
 */
class PlatformThread(thread: Thread) extends TraitThread {
  def getContextClassLoader(): ClassLoader = PlatformThread.emptyClassLoader
}

private object PlatformThread {
  // Scala Native's class loader has no getResources. The placeholder keeps the
  // classpath fallback of include resolution working: it finds no resources.
  val emptyClassLoader: ClassLoader = new ClassLoader {}
}
