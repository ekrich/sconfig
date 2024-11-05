/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException
import scala.jdk.CollectionConverters._

class ValidationTest extends TestUtilsShared {

  @Test
  def validationCatchesUnresolved(): Unit = {
    val reference = parseConfig("""{ a : 2 }""")
    val conf = parseConfig("""{ b : ${c}, c : 42 }""")
    val e = intercept[ConfigException.NotResolved] {
      conf.checkValid(reference)
    }
    assertTrue(
      "expected different message, got: " + e.getMessage,
      e.getMessage.contains("resolve")
    )
  }

  @Test
  def validationCatchesListOverriddenWithNumber(): Unit = {
    val reference = parseConfig("""{ a : [{},{},{}] }""")
    val conf = parseConfig("""{ a : 42 }""")
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference)
    }

    val expecteds = Seq(WrongType("a", 1, "list", "number"))

    checkValidationException(e, expecteds)
  }

  @Test
  def validationCatchesListOverriddenWithDifferentList(): Unit = {
    val reference = parseConfig("""{ a : [true,false,false] }""")
    val conf = parseConfig("""{ a : [42,43] }""")
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference)
    }

    val expecteds = Seq(WrongElementType("a", 1, "boolean", "number"))

    checkValidationException(e, expecteds)
  }

  @Test
  def validationAllowsListOverriddenWithSameTypeList(): Unit = {
    val reference = parseConfig("""{ a : [1,2,3] }""")
    val conf = parseConfig("""{ a : [4,5] }""")
    conf.checkValid(reference)
  }

  @Test
  def validationCatchesListOverriddenWithNoIndexesObject(): Unit = {
    val reference = parseConfig("""{ a : [1,2,3] }""")
    val conf = parseConfig("""{ a : { notANumber: foo } }""")
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference)
    }

    val expecteds = Seq(WrongType("a", 1, "list", "object"))

    checkValidationException(e, expecteds)
  }

  @Test
  def validationAllowsListOverriddenWithIndexedObject(): Unit = {
    val reference = parseConfig("""{ a : [a,b,c] }""")
    val conf = parseConfig("""{ a : { "0" : x, "1" : y } }""")
    conf.checkValid(reference)
    assertEquals(
      "got the sequence from overriding list with indexed object",
      Seq("x", "y"),
      conf.getStringList("a").asScala
    )
  }
}
