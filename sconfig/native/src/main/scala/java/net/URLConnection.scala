package java.net

import org.ekrich.config.impl.PlatformUnsupported

class URLConnection {

  def getLastModified(): scala.Long =
    throw PlatformUnsupported("java.net.URLConnection.getLastModified")

  def connect(): Unit =
    throw PlatformUnsupported("java.net.URLConnection.connect")

  def getContentType(): String =
    throw PlatformUnsupported("java.net.URLConnection.getContentType")

  def getInputStream(): java.io.InputStream =
    throw PlatformUnsupported("java.net.URLConnection.getInputStream")

  def setRequestProperty(key: String, value: String): Unit =
    throw PlatformUnsupported("java.net.URLConnection.setRequestProperty")
}
