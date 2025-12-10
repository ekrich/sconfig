package org.ekrich.config.impl

import org.ekrich.config.{
  ConfigFactory,
  ConfigFormatOptions,
  ConfigParseOptions,
  ConfigRenderOptions
}
import org.junit.*

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
}
