package java.net

import java.io.InputStream

import org.ekrich.config.impl.PlatformUnsupported

// only for linking Scala.js 1.x
trait URLConnection { // abstract class
  def setRequestProperty(key: String, value: String): Unit =
    throw PlatformUnsupported("java.net.URLConnection.setRequestProperty")

  def connect(): Unit =
    throw PlatformUnsupported("java.net.URLConnection.connect") // abstract

  def getContentType(): String =
    throw PlatformUnsupported("java.net.URLConnection.getContentType")

  def getInputStream(): InputStream =
    throw PlatformUnsupported("java.net.URLConnection.getInputStream")
}
