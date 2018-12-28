package test

import org.ekrich.config.ConfigFactory
import utest._

object Tester extends TestSuite {
  val tests = Tests {
    'load - {
      println("Testing sconfig")
      // example of how system properties override; note this
      // must be set before the config lib is used
      System.setProperty("simple-lib.whatever",
                         "This value comes from a system property")

      // Load our own config values from the default location, application.conf
      val conf = ConfigFactory.load()
      println("The answer is: " + conf.getString("simple-app.answer"))
    }
  }
}
