package java.net

// from Scala Native with @stub removed
class URL(from: String) {

  def getPath(): java.lang.String = ???

  def getProtocol(): java.lang.String = ???

  def openConnection(): java.net.URLConnection = ???

  def openStream(): java.io.InputStream = ???

  override def hashCode: Int = ???

  def toURI(): java.net.URI = ???

  def toExternalForm(): java.lang.String = ???

  // added
  def getFile(): String = ???
}

