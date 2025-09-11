/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._

import java.io.Reader
import java.io.StringReader
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Callable

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.reflect.classTag

import org.ekrich.config._
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigSyntax

abstract trait TestUtils {
  protected def intercept[E <: Throwable: ClassTag](block: => Any): E = {
    val expectedClass = classTag[E].runtimeClass
    var thrown: Option[Throwable] = None
    val result =
      try {
        Some(block)
      } catch {
        case t: Throwable =>
          thrown = Some(t)
          None
      }
    thrown match {
      case Some(t) if expectedClass.isAssignableFrom(t.getClass) =>
        t.asInstanceOf[E]
      case Some(t) =>
        throw new Exception(
          s"Expected exception ${expectedClass.getName} was not thrown, got $t",
          t
        )
      case None =>
        throw new Exception(
          s"Expected exception ${expectedClass.getName} was not thrown, no exception was thrown and got result $result"
        )
    }
  }

  protected def describeFailure[A](desc: String)(code: => A): A = {
    try {
      code
    } catch {
      case t: Throwable =>
        println("Failure on: '%s'".format(desc))
        throw t
    }
  }

  private class NotEqualToAnythingElse {
    override def equals(other: Any) = {
      other match {
        case x: NotEqualToAnythingElse => true
        case _                         => false
      }
    }

    override def hashCode() = 971
  }

  private object notEqualToAnything extends NotEqualToAnythingElse

  private def checkNotEqualToRandomOtherThing(a: Any): Unit = {
    assertFalse(a.equals(notEqualToAnything))
    assertFalse(notEqualToAnything.equals(a))
  }

  protected def checkNotEqualObjects(a: Any, b: Any): Unit = {
    assertNotEquals(a, b)
    assertNotEquals(b, a)
    // hashcode inequality isn't guaranteed, but
    // as long as it happens to work it might
    // detect a bug (if hashcodes are equal,
    // check if it's due to a bug or correct
    // before you remove this)
    assertFalse(a.hashCode() == b.hashCode())
    checkNotEqualToRandomOtherThing(a)
    checkNotEqualToRandomOtherThing(b)
  }

  protected def checkEqualObjects(a: Any, b: Any): Unit = {
    assertEquals(a, b)
    assertEquals(b, a)
    assertTrue(a.hashCode() == b.hashCode())
    checkNotEqualToRandomOtherThing(a)
    checkNotEqualToRandomOtherThing(b)
  }

  private val hexDigits = {
    val a = new Array[Char](16)
    var i = 0
    for (c <- '0' to '9') {
      a(i) = c
      i += 1
    }
    for (c <- 'A' to 'F') {
      a(i) = c
      i += 1
    }
    a
  }

  private def encodeLegibleBinary(bytes: Array[Byte]): String = {
    val sb = new java.lang.StringBuilder()
    for (b <- bytes) {
      if ((b >= 'a' && b <= 'z') ||
          (b >= 'A' && b <= 'Z') ||
          (b >= '0' && b <= '9') ||
          b == '-' || b == ':' || b == '.' || b == '/' || b == ' ') {
        sb.append('_')
        sb.appendCodePoint(b.asInstanceOf[Char])
      } else {
        sb.appendCodePoint(hexDigits((b & 0xf0) >> 4))
        sb.appendCodePoint(hexDigits(b & 0x0f))
      }
    }
    sb.toString
  }

  private def decodeLegibleBinary(s: String): Array[Byte] = {
    val a = new Array[Byte](s.length() / 2)
    var i = 0
    var j = 0
    while (i < s.length()) {
      val sub = s.substring(i, i + 2)
      i += 2
      if (sub.charAt(0) == '_') {
        a(j) = charWrapper(sub.charAt(1)).byteValue
      } else {
        a(j) = Integer.parseInt(sub, 16).byteValue
      }
      j += 1
    }
    a
  }

  def outputStringLiteral(bytes: Array[Byte]): Unit = {
    val hex = encodeLegibleBinary(bytes)
    outputStringLiteral(hex)
  }

  @tailrec
  final def outputStringLiteral(hex: String): Unit = {
    if (hex.nonEmpty) {
      val (head, tail) = hex.splitAt(80)
      val plus = if (tail.isEmpty) "" else " +"
      System.err.println("\"" + head + "\"" + plus)
      outputStringLiteral(tail)
    }
  }

  // origin() is not part of value equality but is serialized, so
  // we check it separately
  protected def checkEqualOrigins[T](a: T, b: T): Unit = (a, b) match {
    case (obj1: ConfigObject, obj2: ConfigObject) =>
      assertEquals(obj1.origin, obj2.origin)
      for (e <- obj1.entrySet().asScala) {
        checkEqualOrigins(e.getValue(), obj2.get(e.getKey()))
      }
    case (list1: ConfigList, list2: ConfigList) =>
      assertEquals(list1.origin, list2.origin)
      for ((v1, v2) <- list1.asScala zip list2.asScala) {
        checkEqualOrigins(v1, v2)
      }
    case (value1: ConfigValue, value2: ConfigValue) =>
      assertEquals(value1.origin, value2.origin)
    case _ =>
  }

  def fakeOrigin() = {
    SimpleConfigOrigin.newSimple("fake origin")
  }

  def includer = {
    ConfigImpl.defaultIncluder
  }

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
  // spray-json throws so change to false
  private val invalidJsonInvalidConf = List[ParseTest](
    "{",
    "}",
    "[",
    "]",
    ",",
    ParseTest(true, "10"), // value not in array or object
    ParseTest(true, "\"foo\""), // value not in array or object
    "\"", // single quote by itself
    ParseTest(false, "[,]"), // array with just a comma in it
    ParseTest(false, "[,,]"), // array with just two commas in it
    ParseTest(false, "[1,2,,]"), // array with two trailing commas
    ParseTest(false, "[,1,2]"), // array with initial comma
    ParseTest(false, "{ , }"), // object with just a comma in it
    ParseTest(false, "{ , , }"), // object with just two commas in it
    "{ 1,2 }", // object with single values not key-value pair
    ParseTest(false, "{ , \"foo\" : 10 }"), // object starts with comma
    ParseTest(false, "{ \"foo\" : 10 ,, }"), // object has two trailing commas
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
    ParseTest(false, "{ \"foo\" , true }"), // comma instead of colon
    ParseTest(
      false,
      "{ \"foo\" : true \"bar\" : false }"
    ), // missing comma between fields
    "[ 10, }]", // array with } as an element
    "[ 10, {]", // array with { as an element
    "{}x", // trailing invalid token after the root object
    "[]x", // trailing invalid token after the root array
    ParseTest(false, "{}{}"), // trailing token after the root object
    ParseTest(false, "{}true"), // trailing token after the root object
    ParseTest(false, "[]{}"), // trailing valid token after the root array
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
    ParseTest(false, "[ \"foo\nbar\" ]"), // unescaped newline in quoted string
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

  // spray-json throws so change to false
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
    ParseTest(false, "[ 1, 2, 3, ]"), // single trailing comma
    ParseTest(false, "[1,2,3  , ]"), // single trailing comma with whitespace
    ParseTest(false, "[1,2,3\n\n , \n]"), // single trailing comma with newlines
    ParseTest(false, "[1,]"), // single trailing comma with one-element array
    ParseTest(false, "{ \"foo\" : 10, }"), // extra trailing comma
    ParseTest(false, "{ \"a\" : \"b\", }"), // single trailing comma in object
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

  protected def parseObject(s: String) = {
    parseConfig(s).root
  }

  protected def parseConfig(s: String) = {
    val options = ConfigParseOptions.defaults
      .setOriginDescription("test string")
      .setSyntax(ConfigSyntax.CONF)
    ConfigFactory.parseString(s, options).asInstanceOf[SimpleConfig]
  }

  protected def subst(ref: String, optional: Boolean): ConfigReference = {
    val path = Path.newPath(ref)
    new ConfigReference(
      fakeOrigin(),
      new SubstitutionExpression(path, optional)
    )
  }

  protected def subst(ref: String): ConfigReference = {
    subst(ref, false)
  }

  protected def substInString(
      ref: String,
      optional: Boolean
  ): ConfigConcatenation = {
    val path = Path.newPath(ref)
    val pieces = List[AbstractConfigValue](
      stringValue("start<"),
      subst(ref, optional),
      stringValue(">end")
    )
    new ConfigConcatenation(fakeOrigin(), pieces.asJava)
  }

  protected def substInString(ref: String): ConfigConcatenation = {
    substInString(ref, false)
  }

  def tokenTrue = Tokens.newBoolean(fakeOrigin(), true)
  def tokenFalse = Tokens.newBoolean(fakeOrigin(), false)
  def tokenNull = Tokens.newNull(fakeOrigin())
  def tokenUnquoted(s: String) = Tokens.newUnquotedText(fakeOrigin(), s)
  def tokenString(s: String) =
    Tokens.newString(fakeOrigin(), s, "\"" + s + "\"")
  def tokenDouble(d: Double) = Tokens.newDouble(fakeOrigin(), d, "" + d)
  def tokenInt(i: Int) = Tokens.newInt(fakeOrigin(), i, "" + i)
  def tokenLong(l: Long) = Tokens.newLong(fakeOrigin(), l, l.toString())
  def tokenLine(line: Int) = Tokens.newLine(fakeOrigin().withLineNumber(line))
  def tokenCommentDoubleSlash(text: String) =
    Tokens.newCommentDoubleSlash(fakeOrigin(), text)
  def tokenCommentHash(text: String) =
    Tokens.newCommentHash(fakeOrigin(), text)
  def tokenWhitespace(text: String) =
    Tokens.newIgnoredWhitespace(fakeOrigin(), text)

  private def tokenMaybeOptionalSubstitution(
      optional: Boolean,
      expression: Token*
  ) = {
    val l = new java.util.ArrayList[Token]
    for (t <- expression) {
      l.add(t)
    }
    Tokens.newSubstitution(fakeOrigin(), optional, l)
  }

  def tokenSubstitution(expression: Token*) = {
    tokenMaybeOptionalSubstitution(false, expression: _*)
  }

  def tokenOptionalSubstitution(expression: Token*) = {
    tokenMaybeOptionalSubstitution(true, expression: _*)
  }

  // quoted string substitution (no interpretation of periods)
  def tokenKeySubstitution(s: String) = tokenSubstitution(tokenString(s))

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

  def tokenizeAsString(s: String) = {
    Tokenizer.render(tokenize(s))
  }

  def configNodeSimpleValue(value: Token) = {
    new ConfigNodeSimpleValue(value)
  }

  def configNodeKey(path: String) = PathParser.parsePathNode(path)

  def configNodeSingleToken(value: Token) = {
    new ConfigNodeSingleToken(value: Token)
  }

  def configNodeObject(nodes: List[AbstractConfigNode]) = {
    new ConfigNodeObject(nodes.asJavaCollection)
  }

  def configNodeArray(nodes: List[AbstractConfigNode]) = {
    new ConfigNodeArray(nodes.asJavaCollection)
  }

  def configNodeConcatenation(nodes: List[AbstractConfigNode]) = {
    new ConfigNodeConcatenation(nodes.asJavaCollection)
  }

  def nodeColon = new ConfigNodeSingleToken(Tokens.COLON)
  def nodeSpace = new ConfigNodeSingleToken(tokenUnquoted(" "))
  def nodeOpenBrace = new ConfigNodeSingleToken(Tokens.OPEN_CURLY)
  def nodeCloseBrace = new ConfigNodeSingleToken(Tokens.CLOSE_CURLY)
  def nodeOpenBracket = new ConfigNodeSingleToken(Tokens.OPEN_SQUARE)
  def nodeCloseBracket = new ConfigNodeSingleToken(Tokens.CLOSE_SQUARE)
  def nodeComma = new ConfigNodeSingleToken(Tokens.COMMA)
  def nodeLine(line: Integer) = new ConfigNodeSingleToken(tokenLine(line))
  def nodeWhitespace(whitespace: String) =
    new ConfigNodeSingleToken(tokenWhitespace(whitespace))
  def nodeKeyValuePair(key: ConfigNodePath, value: AbstractConfigNodeValue) = {
    val nodes = List(key, nodeSpace, nodeColon, nodeSpace, value)
    new ConfigNodeField(nodes.asJavaCollection)
  }
  def nodeInt(value: Integer) = new ConfigNodeSimpleValue(tokenInt(value))
  def nodeString(value: String) = new ConfigNodeSimpleValue(tokenString(value))
  def nodeLong(value: Long) = new ConfigNodeSimpleValue(tokenLong(value))
  def nodeDouble(value: Double) = new ConfigNodeSimpleValue(tokenDouble(value))
  def nodeTrue = new ConfigNodeSimpleValue(tokenTrue)
  def nodeFalse = new ConfigNodeSimpleValue(tokenFalse)
  def nodeCommentHash(text: String) =
    new ConfigNodeComment(tokenCommentHash(text))
  def nodeCommentDoubleSlash(text: String) =
    new ConfigNodeComment(tokenCommentDoubleSlash(text))
  def nodeUnquotedText(text: String) =
    new ConfigNodeSimpleValue(tokenUnquoted(text))
  def nodeNull = new ConfigNodeSimpleValue(tokenNull)
  def nodeKeySubstitution(s: String) =
    new ConfigNodeSimpleValue(tokenKeySubstitution(s))
  def nodeOptionalSubstitution(expression: Token*) =
    new ConfigNodeSimpleValue(tokenOptionalSubstitution(expression: _*))
  def nodeSubstitution(expression: Token*) =
    new ConfigNodeSimpleValue(tokenSubstitution(expression: _*))

  // this is importantly NOT using Path.newPath, which relies on
  // the parser; in the test suite we are often testing the parser,
  // so we don't want to use the parser to build the expected result.
  def path(elements: String*) = new Path(elements: _*)

  protected class TestClassLoader(
      parent: ClassLoader,
      val additions: Map[String, URL]
  ) extends ClassLoader(parent) {
    override def findResources(name: String) = {
      val other = super.findResources(name).asScala
      additions
        .get(name)
        .map({ url => Iterator(url) ++ other })
        .getOrElse(other)
        .asJavaEnumeration
    }
    override def findResource(name: String) = {
      additions.get(name).getOrElse(null)
    }
  }

  protected def withContextClassLoader[T](
      loader: ClassLoader
  )(body: => T): T = {
    val executor = Executors.newSingleThreadExecutor()
    val f = executor.submit(new Callable[T] {
      override def call(): T = {
        val t = Thread.currentThread()
        val old = t.getContextClassLoader()
        t.setContextClassLoader(loader)
        val result =
          try {
            body
          } finally {
            t.setContextClassLoader(old)
          }
        result
      }
    })
    f.get
  }

  private def printIndented(indent: Int, s: String): Unit = {
    for (i <- 0 to indent)
      System.err.print(' ')
    System.err.println(s)
  }

  protected def showDiff(
      a: ConfigValue,
      b: ConfigValue,
      indent: Int = 0
  ): Unit = {
    if (a != b) {
      if (a.valueType != b.valueType) {
        printIndented(indent, "- " + a.valueType)
        printIndented(indent, "+ " + b.valueType)
      } else if (a.valueType == ConfigValueType.OBJECT) {
        printIndented(indent, "OBJECT")
        val aS = a.asInstanceOf[ConfigObject].asScala
        val bS = b.asInstanceOf[ConfigObject].asScala
        for (aKV <- aS) {
          val bVOption = bS.get(aKV._1)
          if (Some(aKV._2) != bVOption) {
            printIndented(indent + 1, aKV._1)
            if (bVOption.isDefined) {
              showDiff(aKV._2, bVOption.get, indent + 2)
            } else {
              printIndented(indent + 2, "- " + aKV._2)
              printIndented(indent + 2, "+ (missing)")
            }
          }
        }
      } else {
        printIndented(indent, "- " + a)
        printIndented(indent, "+ " + b)
      }
    }
  }

  protected def quoteJsonString(s: String): String =
    ConfigImplUtil.renderJsonString(s)

  sealed abstract class Problem(path: String, line: Int) {
    def check(p: ConfigException.ValidationProblem): Unit = {
      assertEquals("matching path", path, p.path)
      assertEquals("matching line for " + path, line, p.origin.lineNumber)
    }

    protected def assertMessage(
        p: ConfigException.ValidationProblem,
        re: String
    ): Unit = {
      assertTrue(
        "didn't get expected message for " + path + ": got '" + p.problem + "'",
        p.problem.matches(re)
      )
    }
  }

  case class Missing(path: String, line: Int, expected: String)
      extends Problem(path, line) {
    override def check(p: ConfigException.ValidationProblem): Unit = {
      super.check(p)
      val re = "No setting.*" + path + ".*expecting.*" + expected + ".*"
      assertMessage(p, re)
    }
  }

  case class WrongType(path: String, line: Int, expected: String, got: String)
      extends Problem(path, line) {
    override def check(p: ConfigException.ValidationProblem): Unit = {
      super.check(p)
      val re =
        "Wrong value type.*" + path + ".*expecting.*" + expected + ".*got.*" + got + ".*"
      assertMessage(p, re)
    }
  }

  case class WrongElementType(
      path: String,
      line: Int,
      expected: String,
      got: String
  ) extends Problem(path, line) {
    override def check(p: ConfigException.ValidationProblem): Unit = {
      super.check(p)
      val re =
        "List at.*" + path + ".*wrong value type.*expecting.*" + expected + ".*got.*element of.*" + got + ".*"
      assertMessage(p, re)
    }
  }

  protected def checkValidationException(
      e: ConfigException.ValidationFailed,
      expecteds: Seq[Problem]
  ): Unit = {
    val problems =
      e.problems.asScala.toIndexedSeq
        .sortBy(_.path)
        .sortBy(_.origin.lineNumber)

    // for (problem <- problems)
    //   System.err.println(
    //     problem.origin.description + ": " + problem.path + ": " + problem.problem
    //   )

    for ((problem, expected) <- problems zip expecteds) {
      expected.check(problem)
    }
    assertEquals(
      "found expected validation problems, got '" + problems + "' and expected '" + expecteds + "'",
      expecteds.size,
      problems.size
    )
  }
}
