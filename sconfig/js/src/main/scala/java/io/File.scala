package java.io

import java.net.URI

import org.ekrich.config.impl.PlatformUnsupported

class File(path: String) extends Serializable with Comparable[File] {
  def this(parent: File, child: String) =
    this((throw PlatformUnsupported("java.io.File(File, String)")): String)

  def this(uri: URI) =
    this((throw PlatformUnsupported("java.io.File(URI)")): String)

  def compareTo(file: File): Int =
    throw PlatformUnsupported("java.io.File.compareTo")

  def exists(): Boolean =
    throw PlatformUnsupported("java.io.File.exists")

  def getPath(): String = path

  def isAbsolute(): Boolean =
    throw PlatformUnsupported("java.io.File.isAbsolute")

  def toURI(): URI =
    throw PlatformUnsupported("java.io.File.toURI")

  def getName(): String =
    throw PlatformUnsupported("java.io.File.getName")

  def getParentFile(): File =
    throw PlatformUnsupported("java.io.File.getParentFile")
}
