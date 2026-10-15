package org.ekrich.config.impl

import java.{lang => jl}
import org.ekrich.config.ConfigFormatOptions

/**
 * Formatting helpers shared by the `Config` value renderer and the
 * `ConfigDocument` formatter, so both make the same layout choices.
 */
private[impl] object FormatUtil {

  /**
   * One level of indentation: four spaces with doubleIndent, otherwise two.
   * Used by `Config.render` and `ConfigDocument.format`.
   */
  def indentUnit(fmt: ConfigFormatOptions): String =
    if (fmt.getDoubleIndent) "    " else "  "

  def indent(
      sb: jl.StringBuilder,
      depth: Int,
      fmt: ConfigFormatOptions
  ): Unit = {
    val unit = indentUnit(fmt)
    var remaining = depth
    while (remaining > 0) {
      sb.append(unit)
      remaining -= 1
    }
  }

  /**
   * The separator between a key and a non-object value. Used by `Config.render`
   * and `ConfigDocument.format`; only by `format` after 3.0.0, when
   * `Config.render` writes Lightbend's `=`.
   */
  def separator(fmt: ConfigFormatOptions, formatted: Boolean): String =
    if (formatted) {
      if (fmt.getColonAssign) ": " else " = "
    } else {
      if (fmt.getColonAssign) ":" else "="
    }

  /**
   * Joins an already rendered path prefix and key element with a dot. A null or
   * empty prefix means there is no prefix. Used by `simplifyNestedObjects` in
   * `Config.render` and `ConfigDocument.format`; only by `format` after 3.0.0.
   */
  def joinPath(prefix: String, key: String): String =
    if (prefix == null || prefix.isEmpty) key else s"$prefix.$key"

  /**
   * Appends `# comment` and a newline; the caller writes the indentation. Used
   * by `Config.render` only, as in Lightbend; `ConfigDocument.format` keeps
   * comments as written.
   */
  def appendComment(sb: jl.StringBuilder, comment: String): Unit = {
    sb.append('#')
    if (!comment.startsWith(" ")) sb.append(' ')
    sb.append(comment)
    sb.append('\n')
  }
}
