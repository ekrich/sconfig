package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException

// Origin line numbers of values and parse errors, exercised through the public
// parse entry points so these tests run on the JVM, Scala.js and Scala Native.
// The cases below are additions beyond the lightbend/config ports: they pin
// ConfigParser's own line counter (advanceLineNumberBeforeValue) and the
// value origins. Revisit and drop them when upstream adds equivalent coverage.
class OriginLineNumberSharedTest extends TestUtilsShared {
  private def lineNumberTest(num: Int, text: String): Unit = {
    val e = intercept[ConfigException] {
      parseConfig(text)
    }
    if (!e.getMessage.contains(s"$num:"))
      throw new Exception(
        "error message did not contain line '" + num + "' '" + text
          .replace("\n", "\\n") + "'",
        e
      )
  }

  // newlines between a separator and its value are counted too; this error
  // comes from ConfigParser, which tracks lines separately. (The sibling
  // include-url cases stay in the JVM ConfParserTest: URL includes are not
  // implemented on Scala.js.)
  @Test
  def parseErrorAfterSeparatorNewlineNamesTheRightLine(): Unit = {
    lineNumberTest(3, "a =\n  1\nb = [ { c += 2 } ]")
  }

  @Test
  def valuesAfterSeparatorNewlineHaveCorrectOriginLine(): Unit = {
    val scalar = parseConfig("a=\n42")
    assertEquals(2, scalar.getValue("a").origin.lineNumber)

    val objectAfterEquals = parseConfig("a=\n{\n b=1\n}")
    assertEquals(2, objectAfterEquals.getObject("a").origin.lineNumber)

    val objectAfterColon = parseConfig("a:\n{\n b=1\n}")
    assertEquals(2, objectAfterColon.getObject("a").origin.lineNumber)

    val arrayAfterEquals = parseConfig("a=\n[\n 1\n]")
    assertEquals(2, arrayAfterEquals.getList("a").origin.lineNumber)
  }

  @Test
  def valuesAfterMultilineStringHaveCorrectOriginLine(): Unit = {
    val tripleQuote = "\"\"\""
    val conf = parseConfig(
      Seq(
        "transformations {",
        "  trim-xml-leading-whitespace = " + tripleQuote,
        "    stringTransformation {",
        "        println(\"Original message: '$it'\")",
        "        it.trimStart { it.isWhitespace() || it == '\\uFEFF' }.also { println(\"Trimmed leading whitespace: '${it}'\") }",
        "    }",
        "  " + tripleQuote,
        "}",
        "",
        "pipelines {",
        "  my-pipeline {",
        "    from {",
        "      type = GENERATOR",
        "      count = 1",
        "      message = \"foo\"",
        "    }",
        "    error-strategy = SHUTDOWN",
        "    processing {",
        "      transformation = trim-xml-leading-whitespace",
        "      xml-to-json = { attribute-prefix = \"@\" }",
        "      xml-to-json-list = [ { attribute-prefix = \"@\" } ]",
        "    }",
        "    to {",
        "      type = LOGGER",
        "    }",
        "  }",
        "}"
      ).mkString("\n")
    )

    assertEquals(
      19,
      conf
        .getValue("pipelines.my-pipeline.processing.transformation")
        .origin
        .lineNumber
    )
    assertEquals(
      20,
      conf
        .getObject("pipelines.my-pipeline.processing.xml-to-json")
        .origin
        .lineNumber
    )
    assertEquals(
      21,
      conf
        .getList("pipelines.my-pipeline.processing.xml-to-json-list")
        .origin
        .lineNumber
    )
  }

  // not from lightbend/config: pins the tokenize-time origins that list
  // elements carry (recorded by ConfigNodeArray and its node subclasses);
  // revisit and drop it when upstream adds equivalent coverage
  @Test
  def elementsAfterMultilineStringInArrayHaveCorrectOriginLine(): Unit = {
    val tripleQuote = "\"\"\""
    val list = parseConfig(
      s"a = [ ${tripleQuote}x\ny${tripleQuote}, { b = 1 }, [ 2 ] ]"
    ).getList("a")

    assertEquals(2, list.get(1).origin.lineNumber)
    assertEquals(2, list.get(2).origin.lineNumber)
  }
}
