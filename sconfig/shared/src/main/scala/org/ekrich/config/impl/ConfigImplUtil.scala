/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URISyntaxException
import java.net.URL
import java.util as ju
import scala.annotation.varargs
import scala.util.control.Breaks.*
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigSyntax

/**
 * Internal implementation detail, not ABI stable, do not touch. For use only by
 * the [[org.ekrich.config]] package.
 */
object ConfigImplUtil {
  // '.' added as forbidden to disable treating it as path expression
  // paths are represented differently in the inner model
  private val forbiddenUnquotedChars =
    Vector('$', '"', '{', '}', '[', ']', ':', '=', ',', '+', '#', '`', '^', '?',
      '!', '@', '*', '&', '\\', '\u00A0', '\u2007', '\u202F', '\uFEFF', '.')

  def isForbiddenUnquotedChar(c: Char): Boolean =
    forbiddenUnquotedChars.contains(c) || c.isWhitespace

  def equalsHandlingNull(a: AnyRef, b: AnyRef) =
    if (a == null && b != null) false
    else if (a != null && b == null) false
    else if (a eq b) {
      // catches null == null plus optimizes identity case
      true
    } else a == b

  def isC0Control(codepoint: Int) =
    codepoint >= 0x0000 && codepoint <= 0x001f

  def renderJsonString(s: String): String = {
    val sb = new StringBuilder
    sb.append('"')
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      c match {
        case '"' =>
          sb.append("\\\"")
        case '\\' =>
          sb.append("\\\\")
        case '\n' =>
          sb.append("\\n")
        case '\b' =>
          sb.append("\\b")
        case '\f' =>
          sb.append("\\f")
        case '\r' =>
          sb.append("\\r")
        case '\t' =>
          sb.append("\\t")
        case _ =>
          if (isC0Control(c.toInt)) sb.append("\\u%04x".format(c.toInt))
          else sb.append(c)
      }
      i += 1
    }
    sb.append('"')
    sb.toString
  }
  // rules for unquoted: https://github.com/lightbend/config/blob/master/HOCON.md#unquoted-strings
  private[impl] def renderStringUnquotedIfPossible(s: String): String = {
    // this can quote unnecessarily as long as it never fails to quote when necessary
    if (s.length == 0) renderJsonString(s)
    else {
      // if it starts with a hyphen or number, we have to quote
      // to ensure we end up with a string and not a number
      val first = s.codePointAt(0)
      if (Character.isDigit(first) || first == '-') renderJsonString(s)
      else if (s.startsWith("include") || s.startsWith("true") ||
          s.startsWith("false") || s.startsWith("null") || s.contains("//"))
        renderJsonString(s)
      // only unquote if does not contain forbidden char or white spaces
      else if (s.exists(isForbiddenUnquotedChar))
        renderJsonString(s)
      else s
    }
  }

  def isWhitespace(codepoint: Int): Boolean =
    codepoint match {
      // try to hit the most common ASCII ones first, then the nonbreaking
      // spaces that Java brokenly leaves out of isWhitespace.
      case ' '      => true
      case '\n'     => true
      case '\u00A0' => true
      case '\u2007' => true
      case '\u202F' => true
      // this one is the BOM, see
      // http://www.unicode.org/faq/utf_bom.html#BOM
      // we just accept it as a zero-width nonbreaking space.
      case '\uFEFF' =>
        true
      case _ =>
        Character.isWhitespace(codepoint)
    }
  def unicodeTrim(s: String): String = {
    // this is dumb because it looks like there aren't any whitespace
    // characters that need surrogate encoding. But, points for
    // pedantic correctness! It's future-proof or something.
    // String.trim() actually is broken, since there are plenty of
    // non-ASCII whitespace characters.
    val length = s.length
    if (length == 0) return s
    var start = 0
    breakable {
      while (start < length) {
        val c = s.charAt(start)
        if (c == ' ' || c == '\n') start += 1
        else {
          val cp = s.codePointAt(start)
          if (isWhitespace(cp)) start += Character.charCount(cp)
          else break() // break
        }
      }
    }
    var end = length
    breakable {
      while (end > start) {
        val c = s.charAt(end - 1)
        if (c == ' ' || c == '\n') end -= 1
        else {
          var cp = 0
          var delta = 0
          if (Character.isLowSurrogate(c)) {
            cp = s.codePointAt(end - 2)
            delta = 2
          } else {
            cp = s.codePointAt(end - 1)
            delta = 1
          }
          if (isWhitespace(cp)) end -= delta
          else break() // break
        }
      }
    }
    s.substring(start, end)
  }
  def extractInitializerError(e: ExceptionInInitializerError) = {
    val cause = e.getCause
    if (cause != null && cause.isInstanceOf[ConfigException])
      cause.asInstanceOf[ConfigException]
    else throw e
  }
  private[impl] def urlToFile(url: URL): File = {
    // this isn't really right, clearly, but not sure what to do.
    try // this will properly handle hex escapes, etc.
      return new File(url.toURI())
    catch {
      case e: URISyntaxException =>
        // this handles some stuff like file:///c:/Whatever/
        // apparently but mangles handling of hex escapes
        return new File(url.getPath())
      case e: IllegalArgumentException =>
        // file://foo with double slash causes
        // IllegalArgumentException "url has an authority component"
        return new File(url.getPath())
    }
  }
  // add Scala vararg version - this is the one finally called now
  @varargs def joinPath(elements: String*): String =
    new Path(elements: _*).render
  def joinPath(elements: ju.List[String]): String =
    joinPath(elements.toArray(new Array[String](0)).toIndexedSeq: _*)
  def splitPath(path: String): ju.List[String] = {
    var p = Path.newPath(path)
    val elements = new ju.ArrayList[String]
    while (p != null) {
      elements.add(p.first)
      p = p.remainder
    }
    elements
  }
  @throws[IOException]
  def readOrigin(in: ObjectInputStream) =
    SerializedConfigValue.readOrigin(in, null)

  @throws[IOException]
  def writeOrigin(out: ObjectOutputStream, origin: ConfigOrigin): Unit =
    SerializedConfigValue.writeOrigin(
      new DataOutputStream(out),
      origin.asInstanceOf[SimpleConfigOrigin],
      null
    )

  private[impl] def toCamelCase(originalName: String): String = {
    val words = originalName.split("-+")
    val nameBuilder = new StringBuilder(originalName.length)
    words.foreach { word =>
      if (nameBuilder.length == 0) nameBuilder.append(word)
      else {
        nameBuilder.append(word.substring(0, 1).toUpperCase)
        nameBuilder.append(word.substring(1))
      }
    }
    nameBuilder.toString
  }

  /**
   * Guess configuration syntax from given filename.
   *
   * @param filename
   *   configuration filename
   * @return
   *   configuration syntax if a match is found. Otherwise, null.
   */
  def syntaxFromExtension(filename: String): ConfigSyntax =
    if (filename == null) null
    else if (filename.endsWith(".json")) ConfigSyntax.JSON
    else if (filename.endsWith(".conf")) ConfigSyntax.CONF
    else if (filename.endsWith(".properties")) ConfigSyntax.PROPERTIES
    else null
}
