package java.net

/**
 * A `java.net.URL` for Scala Native that supports the `file` protocol.
 *
 * Protocols that the JDK has a handler for are accepted and their connection
 * fails with an `IOException` when it is opened, the same way an unreachable
 * URL fails on the JVM; parsing a config is synchronous and Scala Native has no
 * HTTP client, so fetching is not implemented. A name with no protocol, or with
 * a protocol the JDK does not know (for example a Windows drive letter), is
 * rejected with `MalformedURLException`, which leaves the heuristic include
 * free to treat it as a file name.
 */
class URL(from: String) {
  private val colon = from.indexOf(':')
  if (colon <= 0) throw new MalformedURLException("no protocol: " + from)

  private val scheme = from.substring(0, colon).toLowerCase
  if (!URLProtocols.supports(scheme))
    throw new MalformedURLException("unknown protocol: " + scheme)

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

private[net] object URLProtocols {
  private val known = Set("file", "http", "https", "ftp", "jar")

  def supports(scheme: String): Boolean = known(scheme)
}

class URLConnection(url: URL) {

  def getLastModified(): scala.Long = 0L

  def connect(): Unit = ()

  def getContentType(): String = null

  def getInputStream(): java.io.InputStream =
    if (url.getProtocol() == "file")
      new java.io.FileInputStream(new java.io.File(url.toURI().getPath))
    else
      throw new java.io.IOException(
        "protocol '" + url.getProtocol() + "' is not supported on Scala Native"
      )

  def setRequestProperty(key: String, value: String): Unit = ()
}
