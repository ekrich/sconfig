/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.json4s._
import org.json4s.native.JsonParser

import java.{util => ju}
import scala.jdk.CollectionConverters._
import org.ekrich.config._

class Json4sTest extends TestUtilsJson4s {
  def parse(s: String): ConfigValue = {
    val options = ConfigParseOptions.defaults
      .setOriginDescription("test json string")
      .setSyntax(ConfigSyntax.JSON)
    Parseable.newString(s, options).parseValue()
  }

  def parseAsConf(s: String): ConfigValue = {
    val options = ConfigParseOptions.defaults
      .setOriginDescription("test conf string")
      .setSyntax(ConfigSyntax.CONF)
    Parseable.newString(s, options).parseValue()
  }

  private def toJson(value: ConfigValue): JValue = {
    value match {
      case v: ConfigObject =>
        JObject(
          v.keySet()
            .asScala
            .map(k => JsonAST.JField(k, toJson(v.get(k))))
            .toList
        )
      case v: ConfigList =>
        JArray(v.asScala.toList.map(elem => toJson(elem)))
      case v: ConfigBoolean =>
        JBool(v.unwrapped)
      case v: ConfigInt =>
        JInt(BigInt(v.unwrapped))
      case v: ConfigLong =>
        JInt(BigInt(v.unwrapped))
      case v: ConfigDouble =>
        JDouble(v.unwrapped)
      case v: ConfigString =>
        JString(v.unwrapped)
      case v: ConfigNull =>
        JNull
    }
  }

  private def fromJson(jsonValue: JValue): AbstractConfigValue = {
    jsonValue match {
      case JObject(fields) =>
        val m = new ju.HashMap[String, AbstractConfigValue]()
        fields.foreach(field => m.put(field._1, fromJson(field._2)))
        new SimpleConfigObject(fakeOrigin(), m)
      case JArray(values) =>
        new SimpleConfigList(fakeOrigin(), values.map(fromJson(_)).asJava)
      case JInt(n)    => intValue(n.intValue)
      case JLong(n)   => longValue(n)
      case JDouble(n) => doubleValue(n)
      case JBool(b)   =>
        new ConfigBoolean(fakeOrigin(), b)
      case JString(s) =>
        new ConfigString.Quoted(fakeOrigin(), s)
      case JNull =>
        new ConfigNull(fakeOrigin())
      case JNothing =>
        throw new ConfigException.BugOrBroken(
          "Returned JNothing, probably an empty document (?)"
        )
      case _ =>
        throw new IllegalStateException("Unexpected JValue: " + jsonValue)
    }
  }

  private def withJsonExceptionsConverted[T](block: => T): T = {
    try {
      block
    } catch {
      case e: ParserUtil.ParseException =>
        throw new ConfigException.Parse(
          SimpleConfigOrigin.newSimple("json parser"),
          e.getMessage(),
          e
        )
    }
  }

  // parse a string using the Json Parser's AST. We then test by ensuring we have the same results as
  // the Json parser for a variety of JSON strings.

  private def fromJsonWithJsonParser(json: String): ConfigValue = {
    withJsonExceptionsConverted(fromJson(JsonParser.parse(json)))
  }

  // For string quoting, check behavior of escaping a random character instead of one on the list
  @Test
  def invalidJsonThrows(): Unit = {
    var tested = 0
    // be sure json parser throws on the string
    for (invalid <- whitespaceVariations(invalidJson, false)) {
      if (invalid.jsonBehaviorUnexpected) {
        // json unexpectedly doesn't throw, confirm that
        addOffendingJsonToException("json-nonthrowing", invalid.test) {
          fromJsonWithJsonParser(invalid.test)
        }
      } else {
        addOffendingJsonToException("json", invalid.test) {
          assertThrows(
            classOf[ConfigException],
            () => fromJsonWithJsonParser(invalid.test)
          )
          tested += 1
        }
      }
    }

    assertTrue(tested > 100) // just checking we ran a bunch of tests
    tested = 0

    // be sure we also throw
    for (invalid <- whitespaceVariations(invalidJson, false)) {
      addOffendingJsonToException("config", invalid.test) {
        assertThrows(
          classOf[ConfigException],
          () => parse(invalid.test)
        )
        tested += 1
      }
    }

    assertTrue(tested > 100)
  }

  @Test
  def validJsonWorks(): Unit = {
    var tested = 0

    // be sure we do the same thing as json parser when we build our JSON "DOM"
    for (valid <- whitespaceVariations(validJson, true)) {
      val jsonAST = if (valid.jsonBehaviorUnexpected) {
        SimpleConfigObject.empty
      } else {
        addOffendingJsonToException("json", valid.test) {
          fromJsonWithJsonParser(valid.test)
        }
      }
      val ourAST = addOffendingJsonToException("config-json", valid.test) {
        parse(valid.test)
      }
      val ourConfAST = addOffendingJsonToException("config-conf", valid.test) {
        parseAsConf(valid.test)
      }
      if (valid.jsonBehaviorUnexpected) {
        // ignore this for now
      } else {
        addOffendingJsonToException("config", valid.test) {
          assertEquals(jsonAST, ourAST)
        }
      }

      // check that our parser gives the same result in JSON mode and ".conf" mode.
      // i.e. this tests that ".conf" format is a superset of JSON.
      addOffendingJsonToException("config", valid.test) {
        assertEquals(ourAST, ourConfAST)
      }

      tested += 1
    }

    assertTrue(tested > 100) // just verify we ran a lot of tests
  }

  // Beyond lightbend/config: ConfigDouble quotes non-finite values. Revisit
  // these cases when upstream adds equivalent JSON parser coverage.
  @Test
  def nonFiniteRenderedValuesParseAsJsonStrings(): Unit = {
    for ((number, text) <- Seq(
          (Double.PositiveInfinity, "Infinity"),
          (Double.NegativeInfinity, "-Infinity"),
          (Double.NaN, "NaN")
        )) {
      val value =
        ConfigValueFactory.fromAnyRef(java.lang.Double.valueOf(number))
      val list = ConfigValueFactory.fromIterable(
        ju.Arrays.asList(java.lang.Double.valueOf(number))
      )
      for (options <- Seq(
            ConfigRenderOptions.concise,
            ConfigRenderOptions.defaults
              .setComments(false)
              .setOriginComments(false)
          )) {
        val objectJson = value.atKey("a").root.render(options)
        val listJson = list.atKey("a").root.render(options)
        assertEquals(
          JObject(List("a" -> JString(text))),
          JsonParser.parse(objectJson)
        )
        assertEquals(
          JObject(List("a" -> JArray(List(JString(text))))),
          JsonParser.parse(listJson)
        )
        assertEquals(fromJsonWithJsonParser(objectJson), parse(objectJson))
        assertEquals(fromJsonWithJsonParser(listJson), parse(listJson))
      }
    }
  }

  @Test
  def bareNonFiniteValuesAreRejectedInJson(): Unit = {
    for (text <- Seq("Infinity", "-Infinity", "NaN")) {
      for (input <- Seq("{\"a\":" + text + "}", "{\"a\":[" + text + "]}")) {
        assertThrows(classOf[ConfigException.Parse], () => parse(input))
      }
    }
  }

  @Test
  def renderingJsonStrings(): Unit = {
    def r(s: String) = ConfigImplUtil.renderJsonString(s)
    assertEquals(""""abcdefg"""", r("""abcdefg"""))
    assertEquals(""""\" \\ \n \b \f \r \t"""", r("\" \\ \n \b \f \r \t"))
    // control characters are escaped. Remember that unicode escapes
    // are weird and happen on the source file before doing other processing.
    assertEquals("\"\\" + "u001f\"", r("\u001f"))
  }
}
