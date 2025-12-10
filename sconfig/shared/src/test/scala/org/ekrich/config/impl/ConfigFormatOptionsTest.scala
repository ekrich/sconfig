package org.ekrich.config.impl

import org.junit.*
import org.ekrich.config.ConfigFormatOptions

// tests for new features of Rendering (since fork from lightbend)
class ConfigFormatOptionsTest extends RenderingTestSuite {
  val initialFormatOptions = ConfigFormatOptions.defaults

  @Test
  def noNewLineAtTheEnd(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setNewLineAtEnd(false)
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = "r {}"
    checkEqualsAndStable(expected, result)
  }

  @Test
  def keepOriginOrderOfEntries(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setKeepOriginOrder(true)

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
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useTwoSpacesIndentation(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setDoubleIndent(false)

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
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useColonAsAssignSign(): Unit = {
    implicit val configFormatOptions = initialFormatOptions.setColonAssign(true)

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
    checkEqualsAndStable(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsKeepCommentsOnRoot(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setSimplifyNestedObjects(true)

    val in =
      """// before
        |r {
        | b {
        | d= 42 // middle
        | }
        | }// after""".stripMargin
    val result = formatHocon(in)

    val expected =
      """# before
        |# after
        |r.b {
        |    # middle
        |    d = 42
        |}
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsKeepCommentsNotOnRoot(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setSimplifyNestedObjects(true)

    val in =
      """h: holder
        |// before
        |r {
        | b {
        | d= 42 // middle
        | }
        | }// after""".stripMargin
    val result = formatHocon(in)

    val expected =
      """h = holder
        |# before
        |# after
        |r.b {
        |    # middle
        |    d = 42
        |}
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsOnRoot(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setSimplifyNestedObjects(true)

    val in = """r { "p.at" { d= 42 } }"""
    val result = formatHocon(in)

    val expected =
      """r."p.at".d = 42
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsNotOnRoot(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setSimplifyNestedObjects(true)

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
    checkEqualsAndStable(expected, result)
  }

  @Test
  def simplifyOneEntryNestedObjectsArray(): Unit = {
    implicit val configFormatOptions =
      initialFormatOptions.setSimplifyNestedObjects(true)

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

    checkEqualsAndStable(expected, result)
  }
}
