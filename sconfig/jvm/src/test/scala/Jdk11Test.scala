/**
 *   Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
import org.ekrich.config._
import org.junit.Assert._
import org.junit._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.implicitConversions

/**
 * This is to show how the API works and to be sure it's usable
 * from outside of the library's package and in Scala.
 * It isn't intended to be asserting anything or adding test coverage.
 */
class Jdk11Test {

  val propVersion = System.getProperty("java.version")

  @Test
  def checkVersionFromScala: Unit = {
    val scalaVersion = sys.props.get("java.version")
    assertEquals(Some(propVersion), scalaVersion)
  }

  @Test
  def checkFromConfig(): Unit = {
    val conf = ConfigFactory.load("test01")

    {
      val sysConf     = conf.getConfig("system")
      val javaVersion = sysConf.getAnyRef("javaversion")
      assertEquals(propVersion, javaVersion)
    }

    {
      val javaConf = conf.getConfig("java")
      val version  = javaConf.getAnyRef("version")
      assertEquals(propVersion, version)
    }
  }
}
