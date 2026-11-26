package org.ekrich.config.impl

import java.io.File

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.{ConfigException, ConfigFactory}

/**
 * On Scala Native the library does no IO of its own beyond what `java.io` gives
 * it: a `file()` include still reads the file, but URL and classpath includes
 * fail with a `ConfigException` that names `ConfigParseOptions.setIncluder`.
 */
class IncludeUnsupportedNativeTest {
  private def assertUnsupported(what: String)(body: => Any): Unit =
    try {
      body
      fail("expected a ConfigException for " + what)
    } catch {
      case e: ConfigException =>
        assertTrue(
          what + ": message should name setIncluder but was: " + e.getMessage,
          e.getMessage.contains("setIncluder")
        )
    }

  @Test
  def heuristicInclude(): Unit =
    assertUnsupported("include \"base.conf\"") {
      ConfigFactory.parseString("include \"base.conf\"\na = 1\n")
    }

  @Test
  def urlInclude(): Unit =
    assertUnsupported("include url(\"file:/tmp/base.conf\")") {
      ConfigFactory.parseString(
        "include url(\"file:/tmp/base.conf\")\na = 1\n"
      )
    }

  @Test
  def classpathInclude(): Unit =
    assertUnsupported("include classpath(\"base.conf\")") {
      ConfigFactory.parseString("include classpath(\"base.conf\")\na = 1\n")
    }

  @Test
  def fileIncludeStillWorks(): Unit = {
    val included = new File(
      "sconfig/native/src/test/resources/test03-included.conf"
    )
    assertTrue(
      "test file should exist: " + included.getAbsolutePath,
      included.exists()
    )
    val config = ConfigFactory.parseString(
      "include file(\"" + included.getAbsolutePath + "\")\n"
    )
    assertEquals("This is in the included file", config.getString("foo"))
  }
}
