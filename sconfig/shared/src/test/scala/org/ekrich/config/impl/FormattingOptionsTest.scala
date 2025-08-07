package org.ekrich.config.impl

import org.junit.*
import org.ekrich.config.{
  ConfigFactory,
  ConfigParseOptions,
  ConfigRenderOptions,
  FormattingOptions
}

class FormattingOptionsTest extends TestUtilsShared {
  val parseOptions = ConfigParseOptions.defaults.setAllowMissing(true)
  val myDefaultRenderOptions = ConfigRenderOptions.defaults
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)
    .setFormatted(true)

  def formatHocon(
      str: String
  )(implicit formattingOptions: FormattingOptions): String =
    ConfigFactory
      .parseString(str, parseOptions)
      .root
      .render(myDefaultRenderOptions.setFormattingOptions(formattingOptions))

  @Test
  def noNewLineAtTheEnd(): Unit = {
    implicit val formattingOptions = FormattingOptions(newLineAtEnd = false)
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = "r {}"
    checkEqualObjects(expected, result)
  }

  @Test
  def newLineAtTheEnd(): Unit = {
    implicit val formattingOptions = FormattingOptions()
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = """r {}
                     |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def keepOriginOrderOfEntries(): Unit = {
    implicit val formattingOptions = FormattingOptions(keepOriginOrder = true)

    val in = """r {
               |  p {
               |        s: ${r.ss}
               |     }
               |  f {
               |    s=t_f
               |    n="ALA"
               |  }
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    p {
                     |        s = ${r.ss}
                     |    }
                     |    f {
                     |        s = t_f
                     |        n = ALA
                     |    }
                     |}
                     |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def useTwoSpacesIndentation(): Unit = {
    implicit val formattingOptions = FormattingOptions(doubleIndent = false)

    val in = """r {
               |  p {
               |        d {
               |        s: ${r.ss}
               |        }
               |     }
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |  p {
                     |    d {
                     |      s = ${r.ss}
                     |    }
                     |  }
                     |}
                     |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def useFourSpacesIndentation(): Unit = {
    implicit val formattingOptions = FormattingOptions(doubleIndent = true)

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
    checkEqualObjects(expected, result)
  }

  @Test
  def useColonAsAssignSign(): Unit = {
    implicit val formattingOptions = FormattingOptions(colonAssign = true)

    val in = """r {
               |    s=t_f
               |      n-m=1
               |    n:"ALA"
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    n: ALA
                     |    n-m: 1
                     |    s: t_f
                     |}
                     |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def useEqualsAsAssignSign(): Unit = {
    implicit val formattingOptions = FormattingOptions(colonAssign = false)

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
    checkEqualObjects(expected, result)
  }

  @Test
  def dontSimplifyOneEntryNestedObjects(): Unit = {
    implicit val formattingOptions =
      FormattingOptions(simplifyOneEntryNestedObjects = false)

    val in = """r.p.d= 42"""
    val result = formatHocon(in)

    val expected =
      """r {
        |    p {
        |        d = 42
        |    }
        |}
        |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsOnRoot(): Unit = {
    implicit val formattingOptions =
      FormattingOptions(simplifyOneEntryNestedObjects = true)

    val in = """r { "p.at" { d= 42 } }"""
    val result = formatHocon(in)

    val expected =
      """r."p.at".d = 42
        |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsNotOnRoot(): Unit = {
    implicit val formattingOptions =
      FormattingOptions(simplifyOneEntryNestedObjects = true)

    val in =
      """r { p { "d.ap" { s= 42 } }
        | e {
        |   f= 1
        |   g= 2
        | }}
        | g.d {s=44}""".stripMargin
    val result = formatHocon(in)

    val expected =
      """g.d.s = 44
        |r {
        |    e {
        |        f = 1
        |        g = 2
        |    }
        |    p."d.ap".s = 42
        |}
        |""".stripMargin
    checkEqualObjects(expected, result)
  }
}
