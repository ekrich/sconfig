/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config._

import FileUtils._

/**
 * Most of the File tests should work but internally and externally they use URL
 * and Native doesn't support URL. Once the API use URI internally more test
 * support should be available.
 */
class PublicApiFileTest extends TestUtils {

  // dupe in PublicApiTest
  private def assertNotFound(e: ConfigException): Unit = {
    assertTrue(
      "Message text: " + e.getMessage,
      e.getMessage.contains("No such") ||
        e.getMessage.contains("not found") ||
        e.getMessage.contains("were found") ||
        e.getMessage.contains("java.io.FileNotFoundException")
    )
  }

  @Test
  def allowMissing(): Unit = {
    val e = intercept[ConfigException.IO] {
      ConfigFactory.parseFile(
        resourceFile("nonexistent.conf"),
        ConfigParseOptions.defaults.setAllowMissing(false)
      )
    }
    assertNotFound(e)

    val conf = ConfigFactory.parseFile(
      resourceFile("nonexistent.conf"),
      ConfigParseOptions.defaults.setAllowMissing(true)
    )
    assertTrue("is empty", conf.isEmpty)
  }

  @Test
  def allowMissingFileAnySyntax(): Unit = {
    val e = intercept[ConfigException.IO] {
      ConfigFactory.parseFileAnySyntax(
        resourceFile("nonexistent"),
        ConfigParseOptions.defaults.setAllowMissing(false)
      )
    }
    assertNotFound(e)

    val conf = ConfigFactory.parseFileAnySyntax(
      resourceFile("nonexistent"),
      ConfigParseOptions.defaults.setAllowMissing(true)
    )
    assertTrue("is empty", conf.isEmpty)
  }

  @Test
  def anySyntaxJvmNative(): Unit = {
    // Kept in JVM as anySyntax() this is only a partial test
    // as resource loading not supported yet in Native

    // test01 has all three syntaxes; first load with basename
    val conf = ConfigFactory.parseFileAnySyntax(
      resourceFile("test01"),
      ConfigParseOptions.defaults
    )
    assertEquals(42, conf.getInt("ints.fortyTwo"))
    assertEquals("A", conf.getString("fromJsonA"))
    assertEquals("true", conf.getString("fromProps.bool"))

    // now include a suffix, should only load one of them
    val onlyProps = ConfigFactory.parseFileAnySyntax(
      resourceFile("test01.properties"),
      ConfigParseOptions.defaults
    )
    assertFalse(onlyProps.hasPath("ints.fortyTwo"))
    assertFalse(onlyProps.hasPath("fromJsonA"))
    assertEquals("true", onlyProps.getString("fromProps.bool"))

    // force only one syntax via options
    val onlyPropsViaOptions = ConfigFactory.parseFileAnySyntax(
      resourceFile("test01.properties"),
      ConfigParseOptions.defaults.setSyntax(ConfigSyntax.PROPERTIES)
    )
    assertFalse(onlyPropsViaOptions.hasPath("ints.fortyTwo"))
    assertFalse(onlyPropsViaOptions.hasPath("fromJsonA"))
    assertEquals("true", onlyPropsViaOptions.getString("fromProps.bool"))

    // TODO: continue test when resourse work on native
    // val fromResources = ConfigFactory.parseResourcesAnySyntax(
    //   classOf[PublicApiFileTest],
    //   "/test01",
    //   ConfigParseOptions.defaults
    // )
    // assertEquals(42, fromResources.getInt("ints.fortyTwo"))
    // assertEquals("A", fromResources.getString("fromJsonA"))
    // assertEquals("true", fromResources.getString("fromProps.bool"))
  }

}
