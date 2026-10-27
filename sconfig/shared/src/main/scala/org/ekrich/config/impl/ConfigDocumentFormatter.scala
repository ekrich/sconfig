package org.ekrich.config.impl

import java.{lang => jl}
import java.{util => ju}
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigFormatOptions
import org.ekrich.config.ConfigSyntax

/**
 * Formats the syntax tree of a `ConfigDocument` back to text. Whitespace,
 * newlines, and commas between elements are rebuilt. Keys, values, includes,
 * and comment text are written as they are, so the meaning does not change. See
 * `ConfigDocument.format` for the rules.
 */
private[impl] object ConfigDocumentFormatter {

  def format(
      root: ConfigNodeRoot,
      fmt: ConfigFormatOptions,
      syntax: ConfigSyntax
  ): String = {
    val f = new ConfigDocumentFormatter(fmt, syntax eq ConfigSyntax.JSON)
    f.root(root)
    f.result
  }
}

// Writes one document; ConfigDocumentFormatter.format creates one per call.
final private[impl] class ConfigDocumentFormatter private (
    fmt: ConfigFormatOptions,
    json: Boolean
) {
  private val sb = new jl.StringBuilder
  private var atLineStart = true
  private val simplify = fmt.getSimplifyNestedObjects && !json

  private def result: String = {
    var end = sb.length
    while (end > 0 && Character.isWhitespace(sb.charAt(end - 1))) end -= 1
    sb.setLength(end)
    if (end > 0) sb.append('\n')
    sb.toString
  }

  private def root(node: ConfigNodeRoot): Unit = {
    val children = node.children
    // a root without braces is one object holding the whole document
    if (children.size == 1) children.get(0) match {
      case obj: ConfigNodeObject if !hasOpenBracket(obj) =>
        elements(obj.children, 0, false, true)
      case _ =>
        elements(children, 0, false, true)
    }
    else elements(children, 0, false, true)
  }

  // Writes the fields, includes, values, and comments of a container at
  // depth, rebuilding what is between them. `open` means the container
  // starts with a bracket on the current line.
  private def elements(
      children: ju.List[AbstractConfigNode],
      depth: Int,
      open: Boolean,
      multiLine: Boolean
  ): Unit = {
    var started = false // an element has been written
    var newlines = 0 // newlines since the last element

    def separate(): Unit =
      if (!started) {
        if (multiLine && open) newline()
      } else if (multiLine && newlines > 0) {
        newline()
        if (newlines > 1) newline() // keep one blank line
      } else text(if (json) " " else ", ", depth)

    val it = children.iterator
    while (it.hasNext) {
      it.next() match {
        case c: ConfigNodeComment =>
          if (newlines == 0 && (started || open))
            text(" " + c.render, depth) // a trailing comment
          else {
            separate()
            text(c.render, depth)
            started = true
          }
          newlines = 0
        case t: ConfigNodeSingleToken =>
          if (Tokens.isNewline(t.token)) newlines += 1
          // a HOCON comma is not needed: on one line the separator is
          // rebuilt, and before a newline it is optional
          else if (json && t.token == Tokens.COMMA) text(",", depth)
        // other single tokens are whitespace or this container's brackets
        case f: ConfigNodeField =>
          separate()
          field(f, depth)
          started = true
          newlines = 0
        case i: ConfigNodeInclude =>
          separate()
          text(i.render, depth)
          started = true
          newlines = 0
        case v: AbstractConfigNodeValue =>
          separate()
          value(v, depth)
          started = true
          newlines = 0
        case other =>
          throw new ConfigException.BugOrBroken(
            "unexpected node in a container: " + other
          )
      }
    }
  }

  private def field(f: ConfigNodeField, depth: Int): Unit =
    // joining the lines of this field would put the value in the comment
    if (!f.comments.isEmpty) text(f.render, depth)
    else {
      var path = f.path.render
      var separator = f.separator
      var v = f.value
      if (simplify && separator != Tokens.PLUS_EQUALS) {
        var inner = singleField(v)
        while (inner != null) {
          path = FormatUtil.joinPath(path, inner.path.render)
          separator = inner.separator
          v = inner.value
          inner = singleField(v)
        }
      }
      text(path, depth)
      text(separatorText(separator), depth)
      value(v, depth)
    }

  private def separatorText(separator: Token): String =
    if (separator == null) " " // key { ... }
    else if (separator == Tokens.PLUS_EQUALS) " += "
    else if (json) ": "
    else FormatUtil.separator(fmt, true)

  // The only field of an object that can be pulled up into a dotted path:
  // the object holds nothing else, and the field has no comment or `+=`.
  private def singleField(v: AbstractConfigNodeValue): ConfigNodeField =
    v match {
      case obj: ConfigNodeObject =>
        var found: ConfigNodeField = null
        var count = 0
        val it = obj.children.iterator
        while (it.hasNext) {
          it.next() match {
            case _: ConfigNodeComment     => count += 2
            case _: ConfigNodeSingleToken => // whitespace or brackets
            case f: ConfigNodeField       => found = f; count += 1
            case _                        => count += 2 // an include
          }
        }
        if (count == 1 && found.comments.isEmpty &&
            found.separator != Tokens.PLUS_EQUALS) found
        else null
      case _ => null
    }

  private def value(v: AbstractConfigNodeValue, depth: Int): Unit =
    v match {
      case obj: ConfigNodeObject => container(obj, "{", "}", depth)
      case arr: ConfigNodeArray  => container(arr, "[", "]", depth)
      // simple values and concatenations are written as they are
      case other => text(other.render, depth)
    }

  private def container(
      node: ConfigNodeComplexValue,
      open: String,
      close: String,
      depth: Int
  ): Unit = {
    val children = node.children
    if (!hasElements(children)) text(open + close, depth)
    else if (hasNewline(node)) {
      text(open, depth)
      elements(children, depth + 1, true, true)
      newline()
      text(close, depth)
    } else {
      // objects get spaces inside the braces, arrays do not
      val pad = if (open == "{") " " else ""
      text(open + pad, depth)
      elements(children, depth + 1, false, false)
      text(pad + close, depth)
    }
  }

  private def hasOpenBracket(node: ConfigNodeComplexValue): Boolean =
    !node.children.isEmpty && (node.children.get(0) match {
      case t: ConfigNodeSingleToken =>
        t.token == Tokens.OPEN_CURLY || t.token == Tokens.OPEN_SQUARE
      case _ => false
    })

  private def hasElements(children: ju.List[AbstractConfigNode]): Boolean = {
    val it = children.iterator
    while (it.hasNext) {
      it.next() match {
        case _: ConfigNodeComment     => return true
        case _: ConfigNodeSingleToken => // whitespace or brackets
        case _                        => return true
      }
    }
    false
  }

  private def hasNewline(node: AbstractConfigNode): Boolean = {
    val it = node.tokens.iterator
    while (it.hasNext) if (Tokens.isNewline(it.next())) return true
    false
  }

  private def text(s: String, depth: Int): Unit = {
    if (atLineStart) {
      FormatUtil.indent(sb, depth, fmt)
      atLineStart = false
    }
    sb.append(s)
  }

  // ends the line, dropping any trailing whitespace
  private def newline(): Unit = {
    var end = sb.length
    while (end > 0 && (sb
          .charAt(end - 1) == ' ' || sb.charAt(end - 1) == '\t'))
      end -= 1
    sb.setLength(end)
    sb.append('\n')
    atLineStart = true
  }
}
