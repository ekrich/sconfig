package org.ekrich.config.impl

import org.junit.*
import org.junit.Assert.*
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigFormatOptions
import org.ekrich.config.ConfigParseOptions

import scala.jdk.CollectionConverters.*

// Regression tests for rendering old behaviour compatibility
class ConfigDefaultRenderingTest extends RenderingTestSuite {
  private implicit val defaultFormatOptions: ConfigFormatOptions =
    ConfigFormatOptions.defaults

  @Test
  def newLineAtTheEnd(): Unit = {
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = """r {}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useFourSpacesIndentation(): Unit = {
    val in = """r {
               |  p {
               |        d {
               |        s: ${r.ss}
               |        }
               |     }
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    p {
                     |        d {
                     |            s = ${r.ss}
                     |        }
                     |    }
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useEqualsAsAssignSign(): Unit = {
    val in = """r {
               |    s=t_f
               |      "n-m"=1
               |    n:"ALA"
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    n = ALA
                     |    n-m = 1
                     |    s = t_f
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def dontSimplifyOneEntryNestedObjects(): Unit = {
    val in = """r.p.d= 42"""
    val result = formatHocon(in)

    val expected =
      """r {
        |    p {
        |        d = 42
        |    }
        |}
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def properArrayConcat(): Unit = {
    val in =
      """except: ${ex1} ${ex2}
        |myEmpty: " "
        |""".stripMargin
    val result = formatHocon(in)

    val expected =
      """except = ${ex1} ${ex2}
        |myEmpty = " "
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // an object nested inside an array is never itself "at root" - it always
  // needs its own braces, first entry included. Porting lightbend/config#832.
  @Test
  def listElementsKeepTheirBraces(): Unit = {
    val in = """root = [{foo = bar}, {baz = qux}]"""
    val result = formatHocon(in)

    val expected = """root = [
                     |    {
                     |        foo = bar
                     |    },
                     |    {
                     |        baz = qux
                     |    }
                     |]
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // the space between two substitutions in a concatenation is unquoted in
  // the source; re-quoting it on render turns a valid array concatenation
  // into a parse error on the way back in. Porting lightbend/config#841.
  @Test
  def arrayConcatenationRoundTripsThroughRender(): Unit = {
    val in = """ex1 = [1, 2]
               |except = ${ex1} ${ex1}""".stripMargin
    val result = formatHocon(in)

    val resolved =
      ConfigFactory.parseString(result, ConfigParseOptions.defaults).resolve()
    assertEquals(List(1, 2, 1, 2), resolved.getIntList("except").asScala)
  }
}
