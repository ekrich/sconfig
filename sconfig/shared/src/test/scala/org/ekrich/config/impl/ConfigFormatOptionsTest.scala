package org.ekrich.config.impl

import org.junit.*
import org.ekrich.config.{
  ConfigFactory,
  ConfigParseOptions,
  ConfigRenderOptions,
  ConfigFormatOptions
}
// tests for new features of Rendering (since fork from lightbend)
class ConfigFormatOptionsTest extends TestUtilsShared {
  val parseOptions = ConfigParseOptions.defaults.setAllowMissing(true)
  val myDefaultRenderOptions = ConfigRenderOptions.defaults
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)
    .setFormatted(true)
  val defaultFormatOptions = ConfigFormatOptions.defaults

  def formatHocon(
      str: String
  )(implicit configFormatOptions: ConfigFormatOptions): String =
    ConfigFactory
      .parseString(str, parseOptions)
      .root
      .render(
        myDefaultRenderOptions.setConfigFormatOptions(configFormatOptions)
      )

  @Test
  def noNewLineAtTheEnd(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setNewLineAtEnd(false)
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = "r {}"
    checkEqualObjects(expected, result)
  }

  @Test
  def keepOriginOrderOfEntries(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setKeepOriginOrder(true)

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
    implicit val configFormatOptions =
      defaultFormatOptions.setDoubleIndent(false)

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
  def useColonAsAssignSign(): Unit = {
    implicit val configFormatOptions = defaultFormatOptions.setColonAssign(true)

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
  def simplifyOneEntryNestedObjectsKeepComments(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setSimplifyNestedObjects(true)

    val in =
      """r {
        | // before
        | b {
        | d= 42 // middle
        | }
        | }""".stripMargin
    val result = formatHocon(in)

    val expected =
      """# before
        |# middle
        |r.b.d = 42
        |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsOnRoot(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setSimplifyNestedObjects(true)

    val in = """r { "p.at" { d= 42 } }"""
    val result = formatHocon(in)

    val expected =
      """r."p.at".d = 42
        |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsNotOnRoot(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setSimplifyNestedObjects(true)

    val in =
      """r { p { "d.ap" { s= 42 } }
        | e.h {
        |   f= 1
        |   foo.bar= 2
        | }}
        | g.d {s=44}""".stripMargin
    val result = formatHocon(in)

    val expected =
      """g.d.s = 44
        |r {
        |    e.h {
        |        f = 1
        |        foo.bar = 2
        |    }
        |    p."d.ap".s = 42
        |}
        |""".stripMargin
    checkEqualObjects(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsArray(): Unit = {
    implicit val configFormatOptions =
      defaultFormatOptions.setSimplifyNestedObjects(true)

    val in =
      """r { ma: [ { si.foo: so } ]
        | kio: 1
        | }""".stripMargin
    val result = formatHocon(in)

    val expected =
      """r {
        |    kio = 1
        |    ma = [
        |        { si.foo = so }
        |    ]
        |}
        |""".stripMargin

    checkEqualObjects(expected, result)
  }

}
