package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException

// Parser errors must name the line of the offending statement even when the
// newlines between it and the previous value are embedded in a multiline
// string token. See https://github.com/ekrich/sconfig/issues/625
class ParserErrorOriginTest extends TestUtilsShared {
  private val plusEqualsListMessage =
    "Due to current limitations of the config parser, += does not work nested inside a list. " +
      "+= expands to a ${} substitution and the path in ${} cannot currently refer to list elements. " +
      "You might be able to move the += outside of the list and then refer to it from inside the list with ${}."

  @Test
  def parseErrorAfterMultilineStringNamesOffendingLine(): Unit = {
    // the field with += is on line 7
    val text = Seq(
      "a = [",
      "  {",
      "    x = \"\"\"one",
      " two",
      " three",
      "\"\"\"",
      "    y += 1",
      "  }",
      "]"
    ).mkString("\n")
    val e = intercept[ConfigException.Parse](parseConfig(text))
    assertEquals(7, e.origin.lineNumber)
    assertEquals("test string: 7: " + plusEqualsListMessage, e.getMessage)
  }

  @Test
  def parseErrorAfterSeveralMultilineStringsNamesOffendingLine(): Unit = {
    // the field with += is on line 6
    val text = Seq(
      "x = \"\"\"one",
      " two\"\"\"",
      "y = \"\"\"three",
      " four\"\"\"",
      "a = [",
      "  { b += 1 }",
      "]"
    ).mkString("\n")
    val e = intercept[ConfigException.Parse](parseConfig(text))
    assertEquals(6, e.origin.lineNumber)
    assertEquals("test string: 6: " + plusEqualsListMessage, e.getMessage)
  }

  @Test
  def parseErrorAfterMultilineStringWithCrLfNamesOffendingLine(): Unit = {
    val text = Seq(
      "x = \"\"\"one",
      " two\"\"\"",
      "y = \"\"\"three",
      " four\"\"\"",
      "a = [",
      "  { b += 1 }",
      "]"
    ).mkString("\r\n")
    val e = intercept[ConfigException.Parse](parseConfig(text))
    assertEquals(6, e.origin.lineNumber)
    assertEquals("test string: 6: " + plusEqualsListMessage, e.getMessage)
  }

  @Test
  def parseErrorAfterSeparatorNewlinesNamesOffendingLine(): Unit = {
    // no multiline strings here, so the line counter was already correct;
    // this guards the error origin against regressing that case
    val text = Seq(
      "x = 1",
      "y = 2",
      "a = [",
      "  { b += 1 }",
      "]"
    ).mkString("\n")
    val e = intercept[ConfigException.Parse](parseConfig(text))
    assertEquals(4, e.origin.lineNumber)
    assertEquals("test string: 4: " + plusEqualsListMessage, e.getMessage)
  }
}
