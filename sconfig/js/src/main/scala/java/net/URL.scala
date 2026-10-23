package java.net

/**
 * A `java.net.URL` for Scala.js that supports the `file` protocol.
 *
 * Other protocols are rejected with a `MalformedURLException` instead of
 * failing later with an unimplemented method: parsing a config is synchronous
 * and a browser or Node.js cannot fetch a URL synchronously without extra
 * machinery, so the caller gets a clear error.
 */
class URL(from: String) {
  private val colon = from.indexOf(':')
  if (colon <= 0) throw new MalformedURLException("no protocol: " + from)

  private val scheme = from.substring(0, colon).toLowerCase
  if (scheme != "file")
    throw new MalformedURLException(
      "protocol '" + scheme + "' is not supported on Scala.js: " + from
    )

  private val uri: URI =
    try new URI(from)
    catch {
      case e: URISyntaxException =>
        throw new MalformedURLException(e.getMessage)
    }

  def getPath(): java.lang.String = uri.getPath

  def getProtocol(): java.lang.String = scheme

  def openConnection(): java.net.URLConnection = new FileURLConnection(this)

  def openStream(): java.io.InputStream = openConnection().getInputStream()

  override def hashCode: Int = from.hashCode

  def toURI(): java.net.URI = uri

  def toExternalForm(): java.lang.String = from

  override def toString(): java.lang.String = from

  def getFile(): String = uri.getPath
}

// only for linking Scala.js 1.x
trait URLConnection { // abstract class
  def setRequestProperty(key: String, value: String): Unit = ()

  def connect(): Unit = ()

  def getContentType(): String = null

  def getInputStream(): java.io.InputStream
}

private[net] final class FileURLConnection(url: URL) extends URLConnection {
  def getInputStream(): java.io.InputStream =
    new java.io.FileInputStream(new java.io.File(url.toURI()))
}
