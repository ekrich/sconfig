package java.io

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

/**
 * A `java.io.FileInputStream` for Scala.js backed by the Node.js `fs` module.
 *
 * The whole file is read when the stream is created, which is enough for
 * `sconfig`'s include resolution and keeps the implementation small.
 */
class FileInputStream(file: File) extends InputStream {
  private val bytes: Uint8Array =
    try
      NodeJs.builtin("fs").readFileSync(file.getPath()).asInstanceOf[Uint8Array]
    catch {
      case e: js.JavaScriptException =>
        throw new FileNotFoundException(
          file.getPath + " (" + e.getMessage + ")"
        )
    }

  private var position: Int = 0

  override def read(): Int = {
    val length = bytes.length
    if (position >= length) -1
    else {
      val byte = bytes(position).asInstanceOf[Int]
      position += 1
      byte & 0xff
    }
  }

  override def available(): Int = bytes.length - position
}
