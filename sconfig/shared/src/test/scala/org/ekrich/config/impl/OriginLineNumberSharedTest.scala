package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

// Origin line numbers of values and parse errors, exercised through the public
// parse entry points so these tests run on the JVM, Scala.js and Scala Native.
class OriginLineNumberSharedTest extends TestUtilsShared {
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
