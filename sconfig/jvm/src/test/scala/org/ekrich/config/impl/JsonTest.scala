/**
 *   Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.util.HashMap

import org.junit.Assert._
import org.junit._

import spray.json._

import org.ekrich.config._

class JsonTest extends TestUtils {

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

  private[this] def toJson(value: ConfigValue): JsValue = {
    import scala.collection.JavaConverters._

    value match {
      case v: ConfigObject =>
        JsObject(
          v.keySet()
            .asScala
            .map(k => (k, toJson(v.get(k))))
            .toMap)
      case v: ConfigList =>
        JsArray(v.asScala.toVector.map(elem => toJson(elem)))
      case v: ConfigBoolean =>
        JsBoolean(v.unwrapped)
      case v: ConfigInt =>
        JsNumber(BigInt(v.unwrapped))
      case v: ConfigLong =>
        JsNumber(BigInt(v.unwrapped))
      case v: ConfigDouble =>
        JsNumber(v.unwrapped)
      case v: ConfigString =>
        JsString(v.unwrapped)
      case v: ConfigNull =>
        JsNull
    }
  }

  private[this] def fromJson(jsonValue: JsValue): AbstractConfigValue = {
    import scala.collection.JavaConverters._

    jsonValue match {
      case JsObject(fields) =>
        val m = new HashMap[String, AbstractConfigValue]()
        fields.foreach(field => m.put(field._1, fromJson(field._2)))
        new SimpleConfigObject(fakeOrigin(), m)
      case JsArray(values) =>
        new SimpleConfigList(fakeOrigin(), values.map(fromJson(_)).asJava)
      case JsNumber(n) =>
        if (n.isValidInt) intValue(n.intValue)
        else if (n.isValidLong) longValue(n.longValue)
        else doubleValue(n.doubleValue)
      case JsBoolean(b) =>
        new ConfigBoolean(fakeOrigin(), b)
      case JsString(s) =>
        new ConfigString.Quoted(fakeOrigin(), s)
      case JsNull =>
        new ConfigNull(fakeOrigin())
      case _ =>
        throw new IllegalStateException("Unexpected JsValue: " + jsonValue)
    }
  }

  private def withJsonExceptionsConverted[T](block: => T): T = {
    try {
      block
    } catch {
      case e: JsonParser.ParsingException =>
        throw new ConfigException.Parse(
          SimpleConfigOrigin.newSimple("json parser"),
          e.getMessage(),
          e)
    }
  }

  // parse a string using the Json Parser's AST. We then test by ensuring we have the same results as
  // the Json parser for a variety of JSON strings.

  private def fromJsonWithJsonParser(json: String): ConfigValue = {
    withJsonExceptionsConverted(fromJson(JsonParser(ParserInput(json))))
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
          intercept[ConfigException] {
            fromJsonWithJsonParser(invalid.test)
          }
          tested += 1
        }
      }

    }

    assertTrue(tested > 100) // just checking we ran a bunch of tests
    tested = 0

    // be sure we also throw
    for (invalid <- whitespaceVariations(invalidJson, false)) {
      addOffendingJsonToException("config", invalid.test) {
        intercept[ConfigException] {
          parse(invalid.test)
        }
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
