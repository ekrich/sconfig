/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.Reader
import java.io.StringReader

import language.implicitConversions
import scala.jdk.CollectionConverters._

import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigSyntax

abstract trait TestUtilsJson4s {

  case class ParseTest(
      jsonBehaviorUnexpected: Boolean,
      whitespaceMatters: Boolean,
      test: String
  )
  object ParseTest {
    def apply(jsonBehaviorUnexpected: Boolean, test: String): ParseTest = {
      ParseTest(jsonBehaviorUnexpected, false, test)
    }
  }
  implicit def string2jsontest(test: String): ParseTest =
    ParseTest(false, test)

  // note: it's important to put {} or [] at the root if you
  // want to test "invalidity reasons" other than "wrong root"
  // if json throws, change to false
  private val invalidJsonInvalidConf = List[ParseTest](
    "{",
    "}",
    "[",
    "]",
    ",",
    ParseTest(false, "10"), // value not in array or object
    ParseTest(false, "\"foo\""), // value not in array or object
    "\"", // single quote by itself
    ParseTest(true, "[,]"), // array with just a comma in it
    ParseTest(true, "[,,]"), // array with just two commas in it
    ParseTest(true, "[1,2,,]"), // array with two trailing commas
    ParseTest(true, "[,1,2]"), // array with initial comma
    ParseTest(true, "{ , }"), // object with just a comma in it
    ParseTest(true, "{ , , }"), // object with just two commas in it
    "{ 1,2 }", // object with single values not key-value pair
    ParseTest(true, "{ , \"foo\" : 10 }"), // object starts with comma
    ParseTest(true, "{ \"foo\" : 10 ,, }"), // object has two trailing commas
    " \"a\" : 10 ,, ", // two trailing commas for braceless root object
    "{ \"foo\" : }", // no value in object
    "{ : 10 }", // no key in object
    ParseTest(false, " \"foo\" : "), // no value in object with no braces
    ParseTest(false, " : 10 "), // no key in object with no braces
    " \"foo\" : 10 } ", // close brace but no open
    " \"foo\" : 10 [ ", // no-braces object with trailing gunk
    "{ \"foo\" }", // no value or colon
    "{ \"a\" : [ }", // [ is not a valid value
    "{ \"foo\" : 10, true }", // non-key after comma
    "{ foo \n bar : 10 }", // newline in the middle of the unquoted key
    "[ 1, \\", // ends with backslash
    // these two problems are ignored by the json tokenizer
    "[:\"foo\", \"bar\"]", // colon in an array
    "[\"foo\" : \"bar\"]", // colon in an array another way
    "[ \"hello ]", // unterminated string
    ParseTest(true, "{ \"foo\" , true }"), // comma instead of colon
    ParseTest(
      true,
      "{ \"foo\" : true \"bar\" : false }"
    ), // missing comma between fields
    "[ 10, }]", // array with } as an element
    "[ 10, {]", // array with { as an element
    "{}x", // trailing invalid token after the root object
    "[]x", // trailing invalid token after the root array
    ParseTest(true, "{}{}"), // trailing token after the root object
    ParseTest(false, "{}true"), // trailing token after the root object
    ParseTest(true, "[]{}"), // trailing valid token after the root array
    ParseTest(false, "[]true"), // trailing valid token after the root array
    "[${]", // unclosed substitution
    "[$]", // '$' by itself
    "[$  ]", // '$' by itself with spaces after
    "[${}]", // empty substitution (no path)
    "[${?}]", // no path with ? substitution
    ParseTest(false, true, "[${ ?foo}]"), // space before ? not allowed
    """{ "a" : [1,2], "b" : y${a}z }""", // trying to interpolate an array in a string
    """{ "a" : { "c" : 2 }, "b" : y${a}z }""", // trying to interpolate an object in a string
    """{ "a" : ${a} }""", // simple cycle
    """[ { "a" : 2, "b" : ${${a}} } ]""", // nested substitution
    "[ = ]", // = is not a valid token in unquoted text
    "[ + ]",
    "[ # ]",
    "[ ` ]",
    "[ ^ ]",
    "[ ? ]",
    "[ ! ]",
    "[ @ ]",
    "[ * ]",
    "[ & ]",
    "[ \\ ]",
    "+=",
    "[ += ]",
    "+= 10",
    "10 +=",
    "[ 10e+3e ]", // "+" not allowed in unquoted strings, and not a valid number
    ParseTest(true, "[ \"foo\nbar\" ]"), // unescaped newline in quoted string
    "[ # comment ]",
    "${ #comment }",
    "[ // comment ]",
    "${ // comment }",
    "{ include \"bar\" : 10 }", // include with a value after it
    "{ include foo }", // include with unquoted string
    "{ include : { \"a\" : 1 } }", // include used as unquoted key
    "a=", // no value
    "a:", // no value with colon
    "a= ", // no value with whitespace after
    "a.b=", // no value with path
    "{ a= }", // no value inside braces
    "{ a: }"
  ) // no value with colon inside braces

  // We'll automatically try each of these with whitespace modifications
  // so no need to add every possible whitespace variation
  protected val validJson = List[ParseTest](
    "{}",
    "[]",
    """{ "foo" : "bar" }""",
    """["foo", "bar"]""",
    """{ "foo" : 42 }""",
    "{ \"foo\"\n : 42 }", // newline after key
    "{ \"foo\" : \n 42 }", // newline after colon
    """[10, 11]""",
    """[10,"foo"]""",
    """{ "foo" : "bar", "baz" : "boo" }""",
    """{ "foo" : { "bar" : "baz" }, "baz" : "boo" }""",
    """{ "foo" : { "bar" : "baz", "woo" : "w00t" }, "baz" : "boo" }""",
    """{ "foo" : [10,11,12], "baz" : "boo" }""",
    """[{},{},{},{}]""",
    """[[[[[[]]]]]]""",
    """[[1], [1,2], [1,2,3], []]""", // nested multiple-valued array
    """{"a":{"a":{"a":{"a":{"a":{"a":{"a":{"a":42}}}}}}}}""",
    "[ \"#comment\" ]", // quoted # comment
    "[ \"//comment\" ]", // quoted // comment
    // this long one is mostly to test rendering
    """{ "foo" : { "bar" : "baz", "woo" : "w00t" }, "baz" : { "bar" : "baz", "woo" : [1,2,3,4], "w00t" : true, "a" : false, "b" : 3.14, "c" : null } }""",
    "{}",
    ParseTest(true, "[ 10e+3 ]")
  )

  // if json throws, change to false
  private val validConfInvalidJson = List[ParseTest](
    "", // empty document
    " ", // empty document single space
    "\n", // empty document single newline
    " \n \n   \n\n\n", // complicated empty document
    "# foo", // just a comment
    "# bar\n", // just a comment with a newline
    "# foo\n//bar", // comment then another with no newline
    """{ "foo" = 42 }""", // equals rather than colon
    """{ "foo" = (42) }""", // value with round braces
    """{ foo { "bar" : 42 } }""", // omit the colon for object value
    """{ foo baz { "bar" : 42 } }""", // omit the colon with unquoted key with spaces
    """ "foo" : 42 """, // omit braces on root object
    """{ "foo" : bar }""", // no quotes on value
    """{ "foo" : null bar 42 baz true 3.14 "hi" }""", // bunch of values to concat into a string
    "{ foo : \"bar\" }", // no quotes on key
    "{ foo : bar }", // no quotes on key or value
    "{ foo.bar : bar }", // path expression in key
    "{ foo.\"hello world\".baz : bar }", // partly-quoted path expression in key
    "{ foo.bar \n : bar }", // newline after path expression in key
    "{ foo  bar : bar }", // whitespace in the key
    "{ true : bar }", // key is a non-string token
    ParseTest(true, """{ "foo" : "bar", "foo" : "bar2" }"""), // dup keys
    ParseTest(true, "[ 1, 2, 3, ]"), // single trailing comma
    ParseTest(true, "[1,2,3  , ]"), // single trailing comma with whitespace
    ParseTest(true, "[1,2,3\n\n , \n]"), // single trailing comma with newlines
    ParseTest(true, "[1,]"), // single trailing comma with one-element array
    ParseTest(true, "{ \"foo\" : 10, }"), // extra trailing comma
    ParseTest(true, "{ \"a\" : \"b\", }"), // single trailing comma in object
    "{ a : b, }", // single trailing comma in object (unquoted strings)
    "{ a : b  \n  , \n }", // single trailing comma in object with newlines
    "a : b, c : d,", // single trailing comma in object with no root braces
    "{ a : b\nc : d }", // skip comma if there's a newline
    "a : b\nc : d", // skip comma if there's a newline and no root braces
    "a : b\nc : d,", // skip one comma but still have one at the end
    "[ foo ]", // not a known token in JSON
    "[ t ]", // start of "true" but ends wrong in JSON
    "[ tx ]",
    "[ tr ]",
    "[ trx ]",
    "[ tru ]",
    "[ trux ]",
    "[ truex ]",
    "[ 10x ]", // number token with trailing junk
    "[ / ]", // unquoted string "slash"
    "{ include \"foo\" }", // valid include
    "{ include\n\"foo\" }", // include with just a newline separating from string
    "{ include\"foo\" }", // include with no whitespace after it
    "[ include ]", // include can be a string value in an array
    "{ foo : include }", // include can be a field value also
    "{ include \"foo\", \"a\" : \"b\" }", // valid include followed by comma and field
    "{ foo include : 42 }", // valid to have a key not starting with include
    "[ ${foo} ]",
    "[ ${?foo} ]",
    "[ ${\"foo\"} ]",
    "[ ${foo.bar} ]",
    "[ abc  xyz  ${foo.bar}  qrs tuv ]", // value concatenation
    "[ 1, 2, 3, blah ]",
    "[ ${\"foo.bar\"} ]",
    "{} # comment",
    "{} // comment",
    """{ "foo" #comment
: 10 }""",
    """{ "foo" // comment
: 10 }""",
    """{ "foo" : #comment
 10 }""",
    """{ "foo" : // comment
 10 }""",
    """{ "foo" : 10 #comment
 }""",
    """{ "foo" : 10 // comment
 }""",
    """[ 10, # comment
 11]""",
    """[ 10, // comment
 11]""",
    """[ 10 # comment
, 11]""",
    """[ 10 // comment
, 11]""",
    """{ /a/b/c : 10 }""", // key has a slash in it
    ParseTest(false, true, "[${ foo.bar}]"), // substitution with leading spaces
    ParseTest(
      false,
      true,
      "[${foo.bar }]"
    ), // substitution with trailing spaces
    ParseTest(
      false,
      true,
      "[${ \"foo.bar\"}]"
    ), // substitution with leading spaces and quoted
    ParseTest(
      false,
      true,
      "[${\"foo.bar\" }]"
    ), // substitution with trailing spaces and quoted
    """[ ${"foo""bar"} ]""", // multiple strings in substitution
    """[ ${foo  "bar"  baz} ]""", // multiple strings and whitespace in substitution
    "[${true}]", // substitution with unquoted true token
    "a = [], a += b", // += operator with previous init
    "{ a = [], a += 10 }", // += in braces object with previous init
    "a += b", // += operator without previous init
    "{ a += 10 }", // += in braces object without previous init
    "[ 10e3e3 ]", // two exponents. this should parse to a number plus string "e3"
    "[ 1-e3 ]", // malformed number should end up as a string instead
    "[ 1.0.0 ]", // two decimals, should end up as a string
    "[ 1.0. ]"
  ) // trailing decimal should end up as a string

  protected val invalidJson = validConfInvalidJson ++ invalidJsonInvalidConf

  protected val invalidConf = invalidJsonInvalidConf

  // .conf is a superset of JSON so validJson just goes in here
  protected val validConf = validConfInvalidJson ++ validJson

  protected def addOffendingJsonToException[R](parserName: String, s: String)(
      body: => R
  ) = {
    try {
      body
    } catch {
      case t: Throwable =>
        val tokens =
          try {
            "tokens: " + tokenizeAsList(s)
          } catch {
            case e: Throwable =>
              "tokenizer failed: " + e.getMessage()
          }
        // don't use AssertionError because it seems to keep Eclipse
        // from showing the causing exception in JUnit view for some reason
        throw new Exception(
          parserName + " parser did wrong thing on '" + s + "', " + tokens,
          t
        )
    }
  }

  protected def whitespaceVariations(
      tests: Seq[ParseTest],
      validInJsonParser: Boolean
  ): Seq[ParseTest] = {
    val variations = List(
      (s: String) => s, // identity
      (s: String) => " " + s,
      (s: String) => s + " ",
      (s: String) => " " + s + " ",
      (s: String) =>
        s.replace(
          " ",
          ""
        ), // this would break with whitespace in a key or value
      (s: String) =>
        s.replace(":", " : "), // could break with : in a key or value
      (s: String) =>
        s.replace(",", " , ") // could break with , in a key or value
    )
    tests flatMap { t =>
      if (t.whitespaceMatters) {
        Seq(t)
      } else {
        val withNonAscii =
          if (t.test.contains(" "))
            Seq(
              ParseTest(validInJsonParser, t.test.replace(" ", "\u2003"))
            ) // 2003 = em space, to test non-ascii whitespace
          else
            Seq()
        withNonAscii ++ (for (v <- variations)
          yield ParseTest(t.jsonBehaviorUnexpected, v(t.test)))
      }
    }
  }

  def fakeOrigin() = {
    SimpleConfigOrigin.newSimple("fake origin")
  }

  // it's important that these do NOT use the public API to create the
  // instances, because we may be testing that the public API returns the
  // right instance by comparing to these, so using public API here would
  // make the test compare public API to itself.
  protected def intValue(i: Int) = new ConfigInt(fakeOrigin(), i, null)
  protected def longValue(l: Long) = new ConfigLong(fakeOrigin(), l, null)
  protected def boolValue(b: Boolean) = new ConfigBoolean(fakeOrigin(), b)
  protected def nullValue() = new ConfigNull(fakeOrigin())
  protected def stringValue(s: String) =
    new ConfigString.Quoted(fakeOrigin(), s)
  protected def doubleValue(d: Double) =
    new ConfigDouble(fakeOrigin(), d, null)

  def tokenize(
      origin: ConfigOrigin,
      input: Reader
  ): java.util.Iterator[Token] = {
    Tokenizer.tokenize(origin, input, ConfigSyntax.CONF)
  }

  def tokenize(input: Reader): java.util.Iterator[Token] = {
    tokenize(SimpleConfigOrigin.newSimple("anonymous Reader"), input)
  }

  def tokenize(s: String): java.util.Iterator[Token] = {
    val reader = new StringReader(s)
    val result = tokenize(reader)
    // reader.close() // can't close until the iterator is traversed, so this tokenize() flavor is inherently broken
    result
  }

  def tokenizeAsList(s: String) = {
    tokenize(s).asScala.toList
  }
}
