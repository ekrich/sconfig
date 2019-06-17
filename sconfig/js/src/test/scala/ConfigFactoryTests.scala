package test

import org.ekrich.config.ConfigFactory
import org.junit.Assert._
import org.junit.Test

// essentially a duplicate of Scala Native Test
class ConfigFactoryTests {

  @Test
  def parseString: Unit = {

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

    assert(conf.getInt("maxColumn") == 100)
    assert(conf.getBoolean("project.git") == true)
  }

  @Test
  def resolveString: Unit = {

    val configStr =
      """
        |_pattern.default.main = "../default/main.conf"
        |core = {
        |  version: 0.1
        |  extends: [
        |    ${_pattern.default.main}
        |  ]
        |}
    """.stripMargin

    val conf  = ConfigFactory.parseString(configStr)
    val rconf = conf.resolve()

    assert(conf.getDouble("core.version") == 0.1)
    assert(conf.getString("core.extends") == "../default/main.conf")
  }
}
