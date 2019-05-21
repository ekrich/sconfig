package java.io

import java.net.URI

class File(path: String) extends Serializable with Comparable[File] {

  def this(parent: File, child: String) = this(??? : String)

  def this(uri: URI) = this(??? : String)

  def compareTo(file: File): Int = ???

  def exists(): Boolean = ???

  def getPath(): String = path

  def isAbsolute(): Boolean = ???

  def toURI(): URI = ???

  def getName(): String = ???

  def getParentFile(): File = ???

}
