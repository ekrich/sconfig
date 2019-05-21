package java.io

class FileNotFoundException(s: String) extends IOException(s) {
  def this() = this(null)
}
