package test

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.ConfigFactory

class ConfigFactoryTests {
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
}
