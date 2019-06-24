package test

import org.junit.Assert._
import org.junit.Test
import org.ekrich.config._

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
        |pattern.default.main = "default"
        |core = {
        |  version: "0.1"
        |  extends: [
        |    ${pattern_default_main}
        |  ]
        |}
    """.stripMargin

    val conf = ConfigFactory.parseString(configStr)
    println(conf.isResolved + "  " + conf)
    val rconf = conf.resolve()
    println(rconf.toString)
    //assert(rconf.getString("version") == "0.1")
    //println(rconf.getList("extends").get(0).render)
    //assert(conf.getList("core.extends").get(0).render == "default")
  }
}
