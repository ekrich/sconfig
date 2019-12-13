package java.net

import java.io.InputStream

// only for linking Scala.js 1.x
trait URLConnection { // abstract class
  def setRequestProperty(key: String, value: String): Unit = ???

  def connect(): Unit = ??? // abstract

  def getContentType(): String = ???

  def getInputStream(): InputStream = ???
}
