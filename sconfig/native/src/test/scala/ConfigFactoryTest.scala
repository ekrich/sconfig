package test

import org.ekrich.config.ConfigFactory

import org.junit.Assert._
import org.junit.Test

import scalanative.junit.utils.AssertThrows._

class ConfigFactoryTest {
  @Test def loadNotImplemented(): Unit = {
    // example of how system properties override; note this
    // must be set before the config lib is used
    val propKey = "simple-prop.whatever"
    val propVal = "This value comes from a system property"
    System.setProperty(propKey, propVal)

    // Load our own config values from the default location, application.conf
    val e = assertThrows(classOf[NotImplementedError], {
      val conf = ConfigFactory.load()
      assert(conf.getString(propKey) == propVal)
    })
  }
}
