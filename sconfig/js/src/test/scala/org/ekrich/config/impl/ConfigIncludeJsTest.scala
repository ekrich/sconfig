package org.ekrich.config.impl

import java.io.File

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigParseOptions

/**
 * Include resolution on Scala.js in a Node.js environment: the default includer
 * uses the Node file system, so `file()` includes, `url("file:...")` includes
 * and includes relative to a parsed file work without any API change.
 */
class ConfigIncludeJsTest {
  private def fs: js.Dynamic = g.require("fs").asInstanceOf[js.Dynamic]

  private lazy val root: String = {
    val tmp =
      g.require("os").tmpdir().asInstanceOf[String] + "/sconfig-include-test"
    fs.mkdirSync(tmp, js.Dynamic.literal(recursive = true))
    tmp
  }

  private def write(name: String, text: String): File = {
    val path = root + "/" + name
    val slash = path.lastIndexOf('/')
    if (slash > 0) {
      val parent = path.substring(0, slash)
      fs.mkdirSync(parent, js.Dynamic.literal(recursive = true))
    }
    fs.writeFileSync(path, text)
    new File(path)
  }

  @Test def heuristicIncludeRelativeToParsedFile(): Unit = {
    write(
      "main.conf",
      """include "base.conf"
        |a = 1
        |""".stripMargin
    )
    write("base.conf", "b = 2\n")
    val config = ConfigFactory.parseFile(new File(root + "/main.conf"))
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
  }

  @Test def nestedRelativeInclude(): Unit = {
    write(
      "nested/main.conf",
      """include "sub/base.conf"
        |a = 1
        |""".stripMargin
    )
    write(
      "nested/sub/base.conf",
      """include "deep.conf"
        |b = 2
        |""".stripMargin
    )
    write("nested/sub/deep.conf", "d = 4\n")
    val config = ConfigFactory.parseFile(new File(root + "/nested/main.conf"))
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
    assertEquals(4, config.getInt("d"))
  }

  @Test def fileIncludeFromString(): Unit = {
    val included = write("from-string.conf", "s = 7\n")
    val config = ConfigFactory.parseString(
      "include file(\"" + included.getPath + "\")\na = 1\n"
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(7, config.getInt("s"))
  }

  @Test def fileUrlInclude(): Unit = {
    val included = write("from-url.conf", "u = 8\n")
    val config = ConfigFactory.parseString(
      "include url(\"" + included.toURI + "\")\na = 1\n"
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(8, config.getInt("u"))
  }

  @Test def missingHeuristicIncludeIsIgnored(): Unit = {
    val config = ConfigFactory.parseString(
      "include \"does-not-exist.conf\"\na = 1\n"
    )
    assertEquals(1, config.getInt("a"))
  }

  @Test def requiredMissingIncludeThrowsConfigException(): Unit = {
    try {
      ConfigFactory.parseString("include required(\"does-not-exist.conf\")\na = 1\n")
      fail("expected a ConfigException for a required missing include")
    } catch {
      case _: ConfigException => ()
    }
  }

  @Test def unsupportedUrlSchemeIsReported(): Unit = {
    try {
      ConfigFactory.parseString(
        "include url(\"http://example.com/other.conf\")\na = 1\n"
      )
      fail("expected a ConfigException for an unsupported URL scheme")
    } catch {
      case _: ConfigException => ()
    }
  }
}
