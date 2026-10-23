package java.net

/**
 * A `java.net.URL` for Scala Native that supports the `file` protocol.
 *
 * Scala Native's javalib has a real `java.io.File`, but no URL stack. Other
 * protocols are rejected with a `MalformedURLException`: parsing a config is
 * synchronous and there is no HTTP client in the standard library, so the
 * caller gets a clear error instead of an unimplemented method.
 */
class URL(from: String) {
  private val colon = from.indexOf(':')
  if (colon <= 0) throw new MalformedURLException("no protocol: " + from)

  private val scheme = from.substring(0, colon).toLowerCase
  if (scheme != "file")
    throw new MalformedURLException(
      "protocol '" + scheme + "' is not supported on Scala Native: " + from
    )

  private val uri: URI =
    try new URI(from)
    catch {
      case e: URISyntaxException =>
        throw new MalformedURLException(e.getMessage)
    }

  def getPath(): java.lang.String = uri.getPath

  def getProtocol(): java.lang.String = scheme

  def openConnection(): java.net.URLConnection = new URLConnection(this)

  def openStream(): java.io.InputStream = openConnection().getInputStream()

  override def hashCode: Int = from.hashCode

  def toURI(): java.net.URI = uri

  def toExternalForm(): java.lang.String = from

  override def toString(): java.lang.String = from

  // added
  def getFile(): String = uri.getPath
}

class URLConnection(url: URL) {

  def getLastModified(): scala.Long = 0L

  def connect(): Unit = ()

  def getContentType(): String = null

  def getInputStream(): java.io.InputStream =
    new java.io.FileInputStream(new java.io.File(url.toURI().getPath))

  def setRequestProperty(key: String, value: String): Unit = ()
}
