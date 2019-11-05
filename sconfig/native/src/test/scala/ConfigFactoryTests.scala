package test

import org.ekrich.config.ConfigFactory
import minitest._
import org.ekrich.config.ConfigRenderOptions

object ConfigFactoryTests extends SimpleTestSuite {
  test("load() check system property throws NotImplementedError") {
    // example of how system properties override; note this
    // must be set before the config lib is used
    val propKey = "simple-prop.whatever"
    val propVal = "This value comes from a system property"
    System.setProperty(propKey, propVal)

    // Load our own config values from the default location, application.conf
    val e = intercept[NotImplementedError] {
      val conf = ConfigFactory.load()
      assert(conf.getString(propKey) == propVal)
    }
  }

  test("parseString(s: String) scalafmt example works") {
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

  test("resolve() to force substitution") {
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
    assert(conf.isResolved == false)
    val rconf = conf.resolve()
    assert(rconf.isResolved == true)
    val pattern = rconf.getList("core.extends").get(0).unwrapped
    assert(pattern == "default")
  }
}
