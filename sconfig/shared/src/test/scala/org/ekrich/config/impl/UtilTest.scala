/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.ekrich.config.ConfigSyntax
import org.junit.Assert._
import org.junit._

class UtilTest extends TestUtilsShared {
  private lazy val supplementaryChars = {
    val sb = new java.lang.StringBuilder()
    val codepoints = Seq(0x2070e, 0x20731, 0x20779, 0x20c53, 0x20c78, 0x20c96,
      0x20ccf, 0x20cd5, 0x20d15, 0x20d7c)
    for (c <- codepoints) {
      sb.appendCodePoint(c)
    }
    assertTrue(sb.length() > codepoints.length)
    sb.toString()
  }

  @Test
  def unicodeTrimSupplementaryChars(): Unit = {
    assertEquals("", ConfigImplUtil.unicodeTrim(""))
    assertEquals("a", ConfigImplUtil.unicodeTrim("a"))
    assertEquals("abc", ConfigImplUtil.unicodeTrim("abc"))
    assertEquals("", ConfigImplUtil.unicodeTrim("   \n   \n  \u00A0 "))
    assertEquals(
      supplementaryChars,
      ConfigImplUtil.unicodeTrim(supplementaryChars)
    )

    val s = " \u00A0 \n  " + supplementaryChars + "  \n  \u00A0 "
    val asciiTrimmed = s.trim()
    val unitrimmed = ConfigImplUtil.unicodeTrim(s)

    assertFalse(asciiTrimmed.equals(unitrimmed))
    assertEquals(supplementaryChars, unitrimmed)
  }

  @Test
  def definitionOfWhitespace(): Unit = {
    assertTrue(ConfigImplUtil.isWhitespace(' '))
    assertTrue(ConfigImplUtil.isWhitespace('\n'))
    // these three are nonbreaking spaces
    assertTrue(ConfigImplUtil.isWhitespace('\u00A0'))
    assertTrue(ConfigImplUtil.isWhitespace('\u2007'))
    assertTrue(ConfigImplUtil.isWhitespace('\u202F'))
    // vertical tab, a weird one
    assertTrue(ConfigImplUtil.isWhitespace('\u000B'))
    // file separator, another weird one
    assertTrue(ConfigImplUtil.isWhitespace('\u001C'))
  }

  @Test
  def equalsThatHandlesNull(): Unit = {
    assertTrue(ConfigImplUtil.equalsHandlingNull(null, null))
    assertFalse(ConfigImplUtil.equalsHandlingNull(new Object(), null))
    assertFalse(ConfigImplUtil.equalsHandlingNull(null, new Object()))
    assertTrue(ConfigImplUtil.equalsHandlingNull("", ""))
  }

  val lotsOfStrings = (invalidJson ++ validConf).map(_.test)

  private def roundtripJson(s: String): Unit = {
    val rendered = ConfigImplUtil.renderJsonString(s)
    val parsed = parseConfig("{ foo: " + rendered + "}").getString("foo")
    assertTrue(
      "String round-tripped through maybe-unquoted escaping '" + s + "' " + s.length +
        " rendering '" + rendered + "' " + rendered.length +
        " parsed '" + parsed + "' " + parsed.length,
      s == parsed
    )
  }

  private def roundtripUnquoted(s: String): Unit = {
    val rendered = ConfigImplUtil.renderStringUnquotedIfPossible(s)
    val parsed = parseConfig("{ foo: " + rendered + "}").getString("foo")
    assertTrue(
      "String round-tripped through maybe-unquoted escaping '" + s + "' " + s.length +
        " rendering '" + rendered + "' " + rendered.length +
        " parsed '" + parsed + "' " + parsed.length,
      s == parsed
    )
  }

  @Test
  def renderJsonString(): Unit = {
    for (s <- lotsOfStrings) {
      roundtripJson(s)
    }
  }

  @Test
  def renderUnquotedIfPossible(): Unit = {
    for (s <- lotsOfStrings) {
      roundtripUnquoted(s)
    }
  }

  @Test
  def syntaxFromExtensionConf(): Unit = {
    assertEquals(
      ConfigSyntax.CONF,
      ConfigImplUtil.syntaxFromExtension("application.conf")
    )
  }

  @Test
  def syntaxFromExtensionJson(): Unit = {
    assertEquals(
      ConfigSyntax.JSON,
      ConfigImplUtil.syntaxFromExtension("application.json")
    )
  }

  @Test
  def syntaxFromExtensionProperties(): Unit = {
    assertEquals(
      ConfigSyntax.PROPERTIES,
      ConfigImplUtil.syntaxFromExtension("application.properties")
    )
  }

  @Test
  def syntaxFromExtensionUnknown(): Unit = {
    assertNull(ConfigImplUtil.syntaxFromExtension("application.exe"))
  }

  @Test
  def syntaxFromExtensionNull(): Unit = {
    assertNull(ConfigImplUtil.syntaxFromExtension(null))
  }
}
