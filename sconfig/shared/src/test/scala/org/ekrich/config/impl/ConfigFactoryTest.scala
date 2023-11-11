package org.ekrich.config.impl

// import java.io.StringReader

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.ConfigFactory

class ConfigFactoryTest {

  // uncomment when Scala.js has Reader
  // val filename = "/test01.properties"
  // val fileStr =
  //   """
  //     |# test01.properties file
  //     |fromProps.abc=abc
  //     |fromProps.one=1
  //     |fromProps.bool=true
  //     |fromProps.specialChars=hello^^
  //     """.stripMargin
  // var test01Reader = new StringReader(fileStr)

  @Test def parseString: Unit = {
    val configStr =
      """
        |maxColumn = 100
        |project.git=true
        |align = none
        |danglingParentheses = true
        |newlines.neverBeforeJsNative = true
        |newlines.sometimesBeforeColonInMethodReturnType = false
        |assumeStandardLibraryStripMargin = true
        """.stripMargin

    val conf = ConfigFactory.parseString(configStr)

    assertEquals(conf.getInt("maxColumn"), 100)
    assertEquals(conf.getBoolean("project.git"), true)
  }

  @Test def resolve: Unit = {
    val configStr =
      """
        |pattern-default-main = default
        |core = {
        |  version: "0.1"
        |  extends: [
        |    ${pattern-default-main}
        |  ]
        |}
    """.stripMargin

    val conf = ConfigFactory.parseString(configStr)
    assertEquals(conf.isResolved, false)
    val rconf = conf.resolve()
    assertEquals(rconf.isResolved, true)
    val pattern = rconf.getList("core.extends").get(0).unwrapped
    assertEquals(pattern, "default")
  }

  // @Test
  // def parse(): Unit = {
  //   val filename = "/test01.properties"
  //   val config = ConfigFactory.parseReader(
  //     test01Reader,
  //     ConfigParseOptions.defaults
  //       .setSyntaxFromFilename(filename)
  //   )
  //   assertEquals("hello^^", config.getString("fromProps.specialChars"))
  // }

  // @Test
  // def parseIncorrectFormat(): Unit = {
  //   val e = assertThrows(
  //     classOf[ConfigException.Parse],
  //     () => ConfigFactory.parseReader(test01Reader)
  //   )
  //   assertTrue(
  //     e.getMessage.contains("Expecting end of input or a comma, got '^'")
  //   )
  // }
}
