package test

import org.ekrich.config.ConfigFactory
import utest._

// essentially a duplicate of Scala Native Test
object ConfigFactoryTests extends TestSuite {
  println("Testing sconfig for Scala.js")
  val tests = Tests {
    // Avoid this test in Scala.js as Scala Native has the following:
    // java.lang.Class$.forName(java.lang.String)java.lang.Class
    // java.lang.Class.getConstructor([java.lang.Class)java.lang.reflect.Constructor
    // and java.util.IdentityHashMap
    // "load() check system property throws NotImplementedError" - {

    //   // example of how system properties override; note this
    //   // must be set before the config lib is used
    //   val propKey = "simple-prop.whatever"
    //   val propVal = "This value comes from a system property"
    //   System.setProperty(propKey, propVal)

    //   // Load our own config values from the default location, application.conf
    //   val e = intercept[NotImplementedError] {
    //     val conf = ConfigFactory.load()
    //     assert(conf.getString(propKey) == propVal)
    //   }
    // }

    "parseString(s: String) scalafmt example works" - {
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
  }
}
