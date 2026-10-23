package java.io

import java.net.URI

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

/**
 * A small `java.io.File` for Scala.js, backed by the Node.js `fs` module.
 *
 * Scala.js has no file system in the browser, so only the methods `sconfig`
 * needs to resolve `include` statements are implemented. Outside Node.js they
 * throw an `UnsupportedOperationException` that says why.
 */
class File(path: String) extends Serializable with Comparable[File] {
  def this(parent: File, child: String) =
    this(
      if (File.isAbsolutePath(child)) child
      else File.join(parent.getPath, child)
    )

  def this(uri: URI) = this(uri.getPath)

  def compareTo(other: File): Int = getPath.compareTo(other.getPath)

  def exists(): Boolean = File.fs.existsSync(getPath).asInstanceOf[Boolean]

  def getPath(): String = path

  def isAbsolute(): Boolean = File.isAbsolutePath(path)

  def toURI(): URI =
    new URI("file", null, File.absolutePath(path), null)

  def getName(): String = {
    val separator = math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'))
    path.substring(separator + 1)
  }

  def getParentFile(): File = {
    val parent = File.parentPath(path)
    if (parent == null) null else new File(parent)
  }

  override def toString(): String = getPath
}

private[io] object File {
  private[io] def fs: js.Dynamic = NodeJs.builtin("fs")

  private[io] def isAbsolutePath(path: String): Boolean =
    path.startsWith("/") || path.startsWith("\\") ||
      (path.length > 2 && path.charAt(1) == ':' &&
      (path.charAt(2) == '/' || path.charAt(2) == '\\'))

  private[io] def join(parent: String, child: String): String =
    if (parent.endsWith("/") || parent.endsWith("\\")) parent + child
    else parent + "/" + child

  private[io] def parentPath(path: String): String = {
    val withoutTrailing =
      if (path.length > 1 && (path.endsWith("/") || path.endsWith("\\")))
        path.substring(0, path.length - 1)
      else path
    val separator = math.max(
      withoutTrailing.lastIndexOf('/'),
      withoutTrailing.lastIndexOf('\\')
    )
    if (separator < 0) null
    else if (separator == 0) withoutTrailing.substring(0, 1)
    else withoutTrailing.substring(0, separator)
  }

  private[io] def absolutePath(path: String): String =
    if (isAbsolutePath(path)) path
    else {
      val cwd =
        NodeJs.process.map(_.cwd().asInstanceOf[String]).getOrElse(".")
      join(cwd, path)
    }
}

/**
 * Access to Node.js built-in modules. A Scala.js program can also run in a
 * browser, where neither `require` nor `process` exists; the error then says
 * that the file system is only available on Node.js.
 */
private[io] object NodeJs {
  private[io] def process: Option[js.Dynamic] = {
    val process = g.process
    if (js.typeOf(process) == "undefined") None
    else Some(process.asInstanceOf[js.Dynamic])
  }

  private[io] def builtin(name: String): js.Dynamic = {
    val require = g.require
    if (js.typeOf(require) == "function")
      require(name).asInstanceOf[js.Dynamic]
    else
      process match {
        case Some(p) =>
          val mainModule = p.mainModule
          if (js.typeOf(mainModule) != "undefined" &&
              js.typeOf(mainModule.require) == "function")
            mainModule.require(name).asInstanceOf[js.Dynamic]
          else if (js.typeOf(p.getBuiltinModule) == "function")
            p.getBuiltinModule(name).asInstanceOf[js.Dynamic]
          else
            throw new UnsupportedOperationException(
              "Node.js module '" + name + "' is not available in this JavaScript environment"
            )
        case None =>
          throw new UnsupportedOperationException(
            "The Node.js module '" + name + "' is not available; sconfig needs Node.js to read files on Scala.js"
          )
      }
  }
}
