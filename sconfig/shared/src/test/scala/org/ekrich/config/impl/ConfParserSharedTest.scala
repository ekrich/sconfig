package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException

// Shared counterpart of the line number tests in the JVM ConfParserTest:
// origin line numbers of values and parse errors, exercised through the public
// parse entry points so these tests run on the JVM, Scala.js and Scala Native.
// valuesAfterSeparatorNewlineHaveCorrectOriginLine is from lightbend/config.
// The parse error case is not: it pins ConfigParser's own line counter
// (advanceLineNumberBeforeValue). Revisit and drop it when upstream adds
// equivalent coverage.
class ConfParserSharedTest extends TestUtilsShared {
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
  // comes from ConfigParser, which tracks lines separately.
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
}
