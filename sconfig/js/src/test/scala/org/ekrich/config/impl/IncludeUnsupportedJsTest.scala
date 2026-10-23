package org.ekrich.config.impl

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.{ConfigException, ConfigFactory}

/**
 * On Scala.js the library does no IO, so every include kind that is not
 * resolved by a caller-supplied [[org.ekrich.config.ConfigIncluder]] must fail
 * with a `ConfigException` that names the way out: `ConfigParseOptions
 * .setIncluder`.
 */
class IncludeUnsupportedJsTest {
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
  def fileInclude(): Unit =
    assertUnsupported("include file(\"base.conf\")") {
      ConfigFactory.parseString("include file(\"base.conf\")\na = 1\n")
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
  def parseFile(): Unit =
    assertUnsupported("ConfigFactory.parseFile(new File(\"x.conf\"))") {
      ConfigFactory.parseFile(new java.io.File("x.conf"))
    }
}
