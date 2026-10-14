package org.ekrich.config.impl

import org.ekrich.config._
import org.ekrich.config.parser._
import org.junit.Assert._
import org.junit.Test
import scala.annotation.nowarn

class ConfigDocumentFormatTest {
  private val defaults = ConfigFormatOptions.defaults
  private val jsonParse =
    ConfigParseOptions.defaults.setSyntax(ConfigSyntax.JSON)

  private def format(
      in: String,
      fmt: ConfigFormatOptions = defaults,
      parse: ConfigParseOptions = ConfigParseOptions.defaults
  ): String =
    ConfigDocumentFactory.parseString(in, parse).format(fmt).render

  // checks the expected text, that formatting it again changes nothing, and
  // that the formatted text parses to the same config
  private def check(
      in: String,
      expected: String,
      fmt: ConfigFormatOptions = defaults,
      parse: ConfigParseOptions = ConfigParseOptions.defaults
  ): Unit = {
    assertEquals(expected, format(in, fmt, parse))
    assertEquals("idempotent", expected, format(expected, fmt, parse))
    assertEquals(
      "same meaning",
      ConfigFactory.parseString(in, parse).root,
      ConfigFactory.parseString(expected, parse).root
    )
  }

  // includes are never loaded, so they can't be checked for meaning
  private def checkText(in: String, expected: String): Unit = {
    assertEquals(expected, format(in))
    assertEquals("idempotent", expected, format(expected))
  }

  // ---- the document is a transform

  @Test
  def originalDocumentUnchanged(): Unit = {
    val in = "a=1\n"
    val doc = ConfigDocumentFactory.parseString(in)
    doc.format(defaults)
    assertEquals(in, doc.render)
  }

  @Test
  def alreadyFormattedUnchanged(): Unit = {
    val in = """a = 1
               |b {
               |  c = [1, 2]
               |}
               |""".stripMargin
    check(in, in)
  }

  // ---- layout

  @Test
  def separatorSpacing(): Unit =
    check("a=1\nb   =   2\nc:3\nd{\n}\n", "a = 1\nb = 2\nc = 3\nd {}\n")

  @Test
  def fieldLinesJoined(): Unit = {
    check("a =\n  1\nb\n{\n  c = 1\n}\n", "a = 1\nb {\n  c = 1\n}\n")
    check("a\n=\n\n1\n", "a = 1\n")
  }

  // joining would put the value inside the comment
  @Test
  def fieldWithCommentNotJoined(): Unit = {
    val in = "a = # note\n  1\n"
    check(in, in)
  }

  @Test
  def plusEqualsKept(): Unit =
    check("a+=1\nb  +=  2\n", "a += 1\nb += 2\n")

  @Test
  def indentNested(): Unit = {
    val in = """a {
               |b {
               |        c = 1
               |   }
               |      d = 2
               |}
               |""".stripMargin
    val expected = """a {
                     |  b {
                     |    c = 1
                     |  }
                     |  d = 2
                     |}
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def bracedRoot(): Unit = {
    val in = """{
               |a = 1
               |    b = 2
               |}""".stripMargin
    val expected = """{
                     |  a = 1
                     |  b = 2
                     |}
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def blankLines(): Unit = {
    val in = """
               |
               |a = 1
               |
               |
               |
               |b {
               |
               |  c = 1
               |
               |}
               |
               |""".stripMargin
    val expected = """a = 1
                     |
                     |b {
                     |  c = 1
                     |}
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def trailingWhitespace(): Unit =
    check("a = 1   \nb = 2\t\n", "a = 1\nb = 2\n")

  @Test
  def endOfFile(): Unit = {
    check("a = 1", "a = 1\n")
    check("a = 1\n\n\n", "a = 1\n")
    checkText("", "")
  }

  @Test
  def lineCommasRemoved(): Unit = {
    val in = """a = 1,
               |b = 2 ,
               |c = [
               |  1,
               |  2,
               |]
               |""".stripMargin
    val expected = """a = 1
                     |b = 2
                     |c = [
                     |  1
                     |  2
                     |]
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def inlineCommasKept(): Unit =
    check("a = 1 ,b = 2\nc = [1,2 ,  3]\n", "a = 1, b = 2\nc = [1, 2, 3]\n")

  @Test
  def inlineObjectsAndArrays(): Unit = {
    check("a = {x=1,y=2}\n", "a = { x = 1, y = 2 }\n")
    check("a {x=1}\n", "a { x = 1 }\n")
    check("a = [ 80,443 ]\n", "a = [80, 443]\n")
    check("a = [ ]\nb = {  }\n", "a = []\nb = {}\n")
    check("a = [{x=1}, {y=2}]\n", "a = [{ x = 1 }, { y = 2 }]\n")
  }

  @Test
  def multiLineContainerBrackets(): Unit = {
    val in = """a = [1,
               |  2, 3]
               |b { x = 1
               |  y = 2 }
               |""".stripMargin
    val expected = """a = [
                     |  1
                     |  2, 3
                     |]
                     |b {
                     |  x = 1
                     |  y = 2
                     |}
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def arrayOfObjects(): Unit = {
    val in = """a = [
               |{
               |x = 1
               |}
               |  { y = 2 }
               |]
               |""".stripMargin
    val expected = """a = [
                     |  {
                     |    x = 1
                     |  }
                     |  { y = 2 }
                     |]
                     |""".stripMargin
    check(in, expected)
  }

  // a Config can't have an array root, so only the text is checked
  @Test
  def rootArray(): Unit =
    checkText("[1,\n2]", "[\n  1\n  2\n]\n")

  // ---- comments

  @Test
  def commentsIndented(): Unit = {
    val in = """# top
               |a {
               |      # inside
               |    b = 1
               |        // slashes
               |}
               |""".stripMargin
    val expected = """# top
                     |a {
                     |  # inside
                     |  b = 1
                     |  // slashes
                     |}
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def commentTextKept(): Unit =
    check("#no space\n//no space\na = 1\n", "#no space\n//no space\na = 1\n")

  @Test
  def trailingComments(): Unit = {
    val in = """a = 1      # one
               |b {   // two
               |  c = 2#three
               |}  # four
               |""".stripMargin
    val expected = """a = 1 # one
                     |b { // two
                     |  c = 2 #three
                     |} # four
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def commentAfterLineComma(): Unit =
    check("a = 1, # one\nb = 2\n", "a = 1 # one\nb = 2\n")

  @Test
  def commentOnlyDocument(): Unit =
    check("  # just a comment", "# just a comment\n")

  @Test
  def headerAndFooterComments(): Unit = {
    val in = """# Copyright header
               |  # line 2
               |
               |
               |app {
               |name = x
               |}
               |# trailing comment at end of file
               |
               |
               |""".stripMargin
    val expected = """# Copyright header
                     |# line 2
                     |
                     |app {
                     |  name = x
                     |}
                     |# trailing comment at end of file
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def commentsAroundBracedRoot(): Unit = {
    val in = """  # before
               |
               |
               |{
               |a = 1
               |}
               |# after""".stripMargin
    val expected = """# before
                     |
                     |{
                     |  a = 1
                     |}
                     |# after
                     |""".stripMargin
    check(in, expected)
  }

  // ---- kept as written

  @Test
  def duplicateKeysAndObjects(): Unit = {
    val in = """a = 1
               |a = 2
               |b { x = 1 }
               |b { y = 2 }
               |""".stripMargin
    check(in, in)
  }

  @Test
  def substitutions(): Unit = {
    val in = """a=${FOO}
               |b=${?FOO}
               |c=${FOO[]}
               |d=${?FOO[]}
               |e=[${FOO},${?BAR[]}]
               |""".stripMargin
    val expected = """a = ${FOO}
                     |b = ${?FOO}
                     |c = ${FOO[]}
                     |d = ${?FOO[]}
                     |e = [${FOO}, ${?BAR[]}]
                     |""".stripMargin
    check(in, expected)
  }

  @Test
  def concatenationsUntouched(): Unit = {
    val in = "a =   foo   bar  \nb = ${x}  ${y}\nc = [1] [2]\n"
    val expected = "a = foo   bar\nb = ${x}  ${y}\nc = [1] [2]\n"
    check(in, expected)
  }

  @Test
  def tripleQuotedUntouched(): Unit = {
    val tq = "\"\"\""
    val in = s"a {\nb = ${tq}line 1\n      line 2  \n${tq}\n}\n"
    val expected = s"a {\n  b = ${tq}line 1\n      line 2  \n${tq}\n}\n"
    check(in, expected)
  }

  @Test
  def quotedKeysKept(): Unit =
    check("\"a.b\"=1\n\"c d\".e=2\n", "\"a.b\" = 1\n\"c d\".e = 2\n")

  @Test
  def includesKept(): Unit = {
    val in = """include "a.conf"
               |b {
               |include file("b.conf")
               |    include classpath("c.conf")
               |  include url("http://example.com/d.conf")
               |include required(file("e.conf"))
               |  x = 1
               |}
               |""".stripMargin
    val expected = """include "a.conf"
                     |b {
                     |  include file("b.conf")
                     |  include classpath("c.conf")
                     |  include url("http://example.com/d.conf")
                     |  include required(file("e.conf"))
                     |  x = 1
                     |}
                     |""".stripMargin
    checkText(in, expected)
  }

  // ---- options

  @Test
  def doubleIndent(): Unit =
    check(
      "a {\nb {\nc = 1\n}\n}\n",
      "a {\n    b {\n        c = 1\n    }\n}\n",
      defaults.setDoubleIndent(true)
    )

  // comments, arrays, and five levels of nesting
  @Test
  def doubleIndentDeep(): Unit = {
    val in = """a {
               |# c
               |b = [
               |{
               |c {
               |d = [1,
               |2]
               |}
               |}
               |]
               |}
               |""".stripMargin
    val expected = """a {
                     |    # c
                     |    b = [
                     |        {
                     |            c {
                     |                d = [
                     |                    1
                     |                    2
                     |                ]
                     |            }
                     |        }
                     |    ]
                     |}
                     |""".stripMargin
    check(in, expected, defaults.setDoubleIndent(true))
  }

  @Test
  def doubleIndentJson(): Unit = {
    val in = """{
               |"a": {
               |"b": [1,
               |2]
               |}
               |}""".stripMargin
    val expected = """{
                     |    "a": {
                     |        "b": [
                     |            1,
                     |            2
                     |        ]
                     |    }
                     |}
                     |""".stripMargin
    check(in, expected, defaults.setDoubleIndent(true), jsonParse)
  }

  @Test
  def colonAssign(): Unit =
    check(
      "a = 1\nb: 2\nc += 3\nd { e = 4 }\nf = { g = 5 }\n",
      "a: 1\nb: 2\nc += 3\nd { e: 4 }\nf: { g: 5 }\n",
      defaults.setColonAssign(true)
    )

  @Test
  @nowarn("cat=deprecation")
  def noEffectOptions(): Unit = {
    val in = "b = 1\na = 2"
    val expected = "b = 1\na = 2\n"
    check(in, expected, defaults.setNewLineAtEnd(false))
    check(in, expected, defaults.setKeepOriginOrder(true))
  }

  private val simplify = defaults.setSimplifyNestedObjects(true)

  @Test
  def simplifyChains(): Unit = {
    check("a { b { c = 1 } }\n", "a.b.c = 1\n", simplify)
    val in = """a {
               |  b {
               |    c = 1
               |  }
               |}
               |""".stripMargin
    check(in, "a.b.c = 1\n", simplify)
    check("a.b { c = 1 }\n", "a.b.c = 1\n", simplify)
    check("\"x.y\" { c = 1 }\n", "\"x.y\".c = 1\n", simplify)
  }

  @Test
  def simplifyStopsAtSeveralFields(): Unit = {
    check("a { b { c = 1, d = 2 } }\n", "a.b { c = 1, d = 2 }\n", simplify)
    val in = """a {
               |  b {
               |    c = 1
               |    d = 2
               |  }
               |}
               |""".stripMargin
    val expected = """a.b {
                     |  c = 1
                     |  d = 2
                     |}
                     |""".stripMargin
    check(in, expected, simplify)
  }

  @Test
  def simplifyNotApplied(): Unit = {
    // a comment, an include, or += anywhere in the chain stops it
    val withComment = "a {\n  # note\n  b = 1\n}\n"
    check(withComment, withComment, simplify)
    val withInclude = "a {\n  include \"x.conf\"\n}\n"
    assertEquals(withInclude, format(withInclude, simplify))
    val withPlusEquals = "a { b += 1 }\n"
    check(withPlusEquals, withPlusEquals, simplify)
    // an empty object has no field to pull up
    check("a {}\n", "a {}\n", simplify)
  }

  // ---- JSON

  @Test
  def jsonIndentAndSpacing(): Unit = {
    val in = """{
               |"a":1,
               |      "b" : {"c":[1,2]},
               |  "d": {
               |"e": true
               |}
               |}""".stripMargin
    val expected = """{
                     |  "a": 1,
                     |  "b": { "c": [1, 2] },
                     |  "d": {
                     |    "e": true
                     |  }
                     |}
                     |""".stripMargin
    check(in, expected, parse = jsonParse)
  }

  // JSON always keeps ':' and is never simplified
  @Test
  def jsonIgnoresColonAssignAndSimplify(): Unit = {
    val in = "{\n  \"a\": { \"b\": 1 }\n}\n"
    check(in, in, simplify, jsonParse)
    check(in, in, defaults.setColonAssign(true), jsonParse)
  }

  // ---- the examples in docs/NEW_FEATURES.md

  @Test
  def docsHoconExample(): Unit = {
    val in = """include "base.conf"
               |app{
               |name="demo"   # the app name
               |ports=[80,443]
               |}""".stripMargin
    val expected = """include "base.conf"
                     |app {
                     |  name = "demo" # the app name
                     |  ports = [80, 443]
                     |}
                     |""".stripMargin
    checkText(in, expected)
  }

  @Test
  def docsJsonExample(): Unit =
    check(
      """{"a":1,"b":{"c":[1,2]}}""",
      "{ \"a\": 1, \"b\": { \"c\": [1, 2] } }\n",
      parse = jsonParse
    )
}
