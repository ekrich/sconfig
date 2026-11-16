package java.net

import org.ekrich.config.impl.PlatformUnsupported

// from Scala Native with @stub removed
class URL(from: String) {
  def getPath(): java.lang.String =
    throw PlatformUnsupported("java.net.URL.getPath")

  def getProtocol(): java.lang.String =
    throw PlatformUnsupported("java.net.URL.getProtocol")

  def openConnection(): java.net.URLConnection =
    throw PlatformUnsupported("java.net.URL.openConnection")

  def openStream(): java.io.InputStream =
    throw PlatformUnsupported("java.net.URL.openStream")

  override def hashCode: Int =
    throw PlatformUnsupported("java.net.URL.hashCode")

  def toURI(): java.net.URI =
    throw PlatformUnsupported("java.net.URL.toURI")

  def toExternalForm(): java.lang.String = from

  override def toString(): java.lang.String = toExternalForm()

  // added
  def getFile(): String =
    throw PlatformUnsupported("java.net.URL.getFile")
}
