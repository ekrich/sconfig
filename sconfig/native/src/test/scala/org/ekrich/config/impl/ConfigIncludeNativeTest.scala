package org.ekrich.config.impl

import java.io.File

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigParseOptions

/**
 * Include resolution on Scala Native: `java.io` is the real Scala Native
 * implementation, so `file()` includes work; `url("file:...")` includes and
 * includes relative to a parsed file work once `java.net.URL` is implemented.
 *
 * The test runs with the repository root as the working directory (see
 * TestPath), so resources are addressed from there.
 */
class ConfigIncludeNativeTest {
  private val resourceDir = "sconfig/native/src/test/resources/include"

  private def resource(name: String): File =
    new File(resourceDir + "/" + name)

  private def absolute(file: File): String = file.getAbsolutePath

  @Test def heuristicIncludeRelativeToParsedFile(): Unit = {
    val config = ConfigFactory.parseFile(resource("main.conf"))
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
    assertEquals(4, config.getInt("d"))
  }

  @Test def fileIncludeFromString(): Unit = {
    val config = ConfigFactory.parseString(
      "include file(\"" + absolute(resource("file.conf")) + "\")\na = 1\n"
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(5, config.getInt("f"))
  }

  @Test def fileUrlInclude(): Unit = {
    val config = ConfigFactory.parseString(
      "include url(\"file:" + absolute(resource("url.conf")) + "\")\na = 1\n"
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(6, config.getInt("u"))
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

  @Test def heuristicHttpIncludeIsNotSilentlyDropped(): Unit = {
    try {
      ConfigFactory.parseString(
        "include \"http://example.com/other.conf\"\na = 1\n"
      )
      fail("a remote include must not be dropped silently")
    } catch {
      case _: ConfigException => ()
    }
  }

  @Test def unsupportedUrlSchemeIsReported(): Unit = {
    try {
      ConfigFactory.parseString(
        "include url(\"http://example.com/other.conf\")\na = 1\n"
      )
      fail("expected a ConfigException for an unsupported URL protocol")
    } catch {
      case e: ConfigException =>
        val message = e.getMessage
        assertTrue(
          "the message must name the protocol, not call it an invalid URL: " + message,
          message != null && !message.contains("invalid URL")
        )
    }
  }

  @Test def windowsLikeNameFallsBackToFile(): Unit = {
    val config = ConfigFactory.parseString("include \"C:/does-not-exist.conf\"\na = 1\n")
    assertEquals(1, config.getInt("a"))
  }
}
