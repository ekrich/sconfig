package org.ekrich.config.impl

import java.io.StringReader

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.{ConfigFactory, ConfigParseOptions, ConfigException}

class ConfigFactoryJvmNativeTests {
  // These tests will be here until JS supports Reader
  // Then they will be removed and then uncommented in shared
  val filename = "/test01.properties"
  val fileStr =
    """
      |# test01.properties file
      |fromProps.abc=abc
      |fromProps.one=1
      |fromProps.bool=true
      |fromProps.specialChars=hello^^
      """.stripMargin
  var test01Reader = new StringReader(fileStr)

  @Test
  def parse(): Unit = {
    val filename = "/test01.properties"
    val config = ConfigFactory.parseReader(
      test01Reader,
      ConfigParseOptions.defaults
        .setSyntaxFromFilename(filename)
    )
    assertEquals("hello^^", config.getString("fromProps.specialChars"))
  }

  @Test
  def parseIncorrectFormat(): Unit = {
    val e = assertThrows(
      classOf[ConfigException.Parse],
      () => ConfigFactory.parseReader(test01Reader)
    )
    assertTrue(
      e.getMessage.contains("Expecting end of input or a comma, got '^'")
    )
  }
}
