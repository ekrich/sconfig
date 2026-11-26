package java.io

import org.ekrich.config.impl.PlatformUnsupported

class FileInputStream(file: File) extends InputStream {
  override def read(): Int =
    throw PlatformUnsupported("java.io.FileInputStream.read")
}
