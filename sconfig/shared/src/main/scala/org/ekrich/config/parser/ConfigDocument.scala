package org.ekrich.config.parser

import org.ekrich.config.ConfigFormatOptions
import org.ekrich.config.ConfigValue

/**
 * Represents an individual HOCON or JSON file, preserving all formatting and
 * syntax details. This can be used to replace individual values and exactly
 * render the original text of the input.
 *
 * <p> Because this object is immutable, it is safe to use from multiple threads
 * and there's no need for "defensive copies."
 *
 * <p> <em>Do not implement interface `ConfigDocument`</em>; it should only be
 * implemented by the config library. Arbitrary implementations will not work
 * because the library internals assume a specific concrete implementation.
 * Also, this interface is likely to grow new methods over time, so third-party
 * implementations will break.
 */
trait ConfigDocument {

  /**
   * Returns a new ConfigDocument that is a copy of the current ConfigDocument,
   * but with the desired value set at the desired path. If the path exists, it
   * will remove all duplicates before the final occurrence of the path, and
   * replace the value at the final occurrence of the path. If the path does not
   * exist, it will be added. If the document has an array as the root value, an
   * exception will be thrown.
   *
   * @param path
   *   the path at which to set the desired value
   * @param newValue
   *   the value to set at the desired path, represented as a string. This
   *   string will be parsed into a ConfigNode using the same options used to
   *   parse the entire document, and the text will be inserted as-is into the
   *   document. Leading and trailing comments, whitespace, or newlines are not
   *   allowed, and if present an exception will be thrown. If a concatenation
   *   is passed in for newValue but the document was parsed with JSON, the
   *   first value in the concatenation will be parsed and inserted into the
   *   ConfigDocument.
   * @return
   *   a copy of the ConfigDocument with the desired value at the desired path
   */
  def withValueText(path: String, newValue: String): ConfigDocument

  /**
   * Returns a new ConfigDocument that is a copy of the current ConfigDocument,
   * but with the desired value set at the desired path. Works like
   * [[#withValueText]], but takes a ConfigValue instead of a string.
   *
   * @param path
   *   the path at which to set the desired value
   * @param newValue
   *   the value to set at the desired path, represented as a ConfigValue. The
   *   rendered text of the ConfigValue will be inserted into the
   *   ConfigDocument.
   * @return
   *   a copy of the ConfigDocument with the desired value at the desired path
   */
  def withValue(path: String, newValue: ConfigValue): ConfigDocument

  /**
   * Returns a new ConfigDocument that is a copy of the current ConfigDocument,
   * but with all values at the desired path removed. If the path does not exist
   * in the document, a copy of the current document will be returned. If there
   * is an array at the root, an exception will be thrown.
   *
   * @param path
   *   the path to remove from the document
   * @return
   *   a copy of the ConfigDocument with the desired value removed from the
   *   document.
   */
  def withoutPath(path: String): ConfigDocument

  /**
   * Returns a boolean indicating whether or not a ConfigDocument has a value at
   * the desired path. null counts as a value for purposes of this check.
   *
   * @param path
   *   the path to check
   * @return
   *   true if the path exists in the document, otherwise false
   */
  def hasPath(path: String): Boolean

  /**
   * Returns a new ConfigDocument with the text formatted. The original
   * ConfigDocument is not modified. Call [[render]] on the result to get the
   * formatted text:
   *
   * {{{
   * val text = ConfigDocumentFactory.parseString(input).format(options).render
   * }}}
   *
   * Formatting only changes layout: indentation, spacing, separators, commas,
   * and blank lines. It keeps the meaning, the order of fields, comments,
   * includes, `+=`, and duplicate keys as written. It never loads includes,
   * resolves substitutions, or reads environment variables, so it works the
   * same on every platform.
   *
   * Of the options, `doubleIndent`, `colonAssign`, and `simplifyNestedObjects`
   * apply. `keepOriginOrder` and `newLineAtEnd` are deprecated and have no
   * effect: the order is always kept and the text always ends with one newline.
   * For JSON documents the separator stays `:` and `simplifyNestedObjects` is
   * ignored, so the result is still JSON.
   *
   * Text inserted later with [[withValueText]] or [[withValue]] is not
   * formatted, so call `format` last.
   *
   * @param options
   *   the formatting options
   * @return
   *   a new ConfigDocument with the formatted text
   * @since 2.2.0
   */
  def format(options: ConfigFormatOptions): ConfigDocument

  /**
   * The original text of the input, modified if necessary with any replaced or
   * added values. The text is written exactly as the document holds it. To
   * format it, call [[format]] first and render the result.
   *
   * @return
   *   the modified original text
   */
  def render: String
}
