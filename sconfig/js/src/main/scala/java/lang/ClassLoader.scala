package java.lang

import java.net.URL

abstract class ClassLoader protected (parent: ClassLoader) {
  protected def this() = this(null) // this is in Scala.js version
  def getResources(name: String): java.util.Enumeration[URL] = ???
}
