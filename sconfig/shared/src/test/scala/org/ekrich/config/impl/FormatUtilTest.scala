package org.ekrich.config.impl

import java.{lang => jl}
import org.ekrich.config.ConfigFormatOptions
import org.junit.Assert._
import org.junit.Test

class FormatUtilTest {
  private val two = ConfigFormatOptions.defaults
  private val four = two.setDoubleIndent(true)
  private val colon = two.setColonAssign(true)

  private def indented(depth: Int, fmt: ConfigFormatOptions): String = {
    val sb = new jl.StringBuilder
    FormatUtil.indent(sb, depth, fmt)
    sb.toString
  }

  @Test
  def indent(): Unit = {
    assertEquals("", indented(0, two))
    assertEquals("    ", indented(2, two))
    assertEquals("        ", indented(2, four))
  }

  @Test
  def separator(): Unit = {
    assertEquals(" = ", FormatUtil.separator(two, true))
    assertEquals("=", FormatUtil.separator(two, false))
    assertEquals(": ", FormatUtil.separator(colon, true))
    assertEquals(":", FormatUtil.separator(colon, false))
  }

  @Test
  def joinPath(): Unit = {
    assertEquals("b", FormatUtil.joinPath(null, "b"))
    assertEquals("b", FormatUtil.joinPath("", "b"))
    assertEquals("a.b", FormatUtil.joinPath("a", "b"))
    assertEquals("a.\"b.c\"", FormatUtil.joinPath("a", "\"b.c\""))
  }

  @Test
  def appendComment(): Unit = {
    def comment(c: String) = {
      val sb = new jl.StringBuilder
      FormatUtil.appendComment(sb, c)
      sb.toString
    }
    assertEquals("# text\n", comment("text"))
    assertEquals("# text\n", comment(" text"))
    assertEquals("#  text\n", comment("  text"))
    assertEquals("# \n", comment(""))
  }
}
