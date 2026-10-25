package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException

// Origin line numbers of values and parse errors, exercised through the public
// parse entry points so these tests run on the JVM, Scala.js and Scala Native.
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
  // comes from ConfigParser, which tracks lines separately
  @Test
  def parseErrorAfterSeparatorNewlineNamesTheRightLine(): Unit = {
    lineNumberTest(3, "a =\n  1\nb = [ { c += 2 } ]")
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

  @Test
  def elementsAfterMultilineStringInArrayHaveCorrectOriginLine(): Unit = {
    val tripleQuote = "\"\"\""
    val list = parseConfig(
      s"a = [ ${tripleQuote}x\ny${tripleQuote}, { b = 1 }, [ 2 ] ]"
    ).getList("a")

    assertEquals(2, list.get(1).origin.lineNumber)
    assertEquals(2, list.get(2).origin.lineNumber)
  }

  // With the tokenize-time origins of the multiline string fix in place the
  // object and array cases below pass even without the separator fix (the
  // captured origin already carries the right line); the parse-error cases
  // above are what still pin ConfigParser's own line counter.
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
}
