package java.io

// copied from Scala Native and minimized
class FileInputStream(fd: FileDescriptor, file: Option[File])
    extends InputStream {
  def this(file: File) = this(??? : FileDescriptor, Some(file))

  override def read(): Int = ???
}
