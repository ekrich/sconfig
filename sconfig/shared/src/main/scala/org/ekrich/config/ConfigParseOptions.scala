/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import org.ekrich.config.impl.ConfigImplUtil
import org.ekrich.config.impl.PlatformThread

/**
 * A set of options related to parsing.
 *
 * This object is immutable, so the "setters" return a new object.
 *
 * Here is an example of creating a custom `ConfigParseOptions`:
 *
 * {{{
 * val options = ConfigParseOptions.defaults()
 *   .setSyntax(ConfigSyntax.JSON)
 *   .setAllowMissing(false)
 * }}}
 */
object ConfigParseOptions {

  /**
   * Gets an instance of `ConfigParseOptions` with all fields set to the default
   * values. Start with this instance and make any changes you need.
   *
   * @return
   *   the default parse options
   */
  def defaults =
    new ConfigParseOptions(null, null, true, null, null, false)
}

final class ConfigParseOptions private (
    private val _syntax: ConfigSyntax,
    private val _originDescription: String,
    private val _allowMissing: Boolean,
    private val _includer: ConfigIncluder,
    private val _classLoader: ClassLoader,
    private val _keepDetachedComments: Boolean
) {

  @deprecated(
    "Use getSyntax",
    "Since 1.12.0, will remove in 1.14.0"
  )
  def syntax = _syntax

  @deprecated(
    "Use getOriginDescription",
    "Since 1.12.0, will remove in 1.14.0"
  )
  def originDescription = _originDescription

  @deprecated(
    "Use getAllowMissing",
    "Since 1.12.0, will remove in 1.14.0"
  )
  def allowMissing = _allowMissing

  @deprecated(
    "Use getIncluder",
    "Since 1.12.0, will remove in 1.14.0"
  )
  def includer = _includer

  @deprecated(
    "Use getClassLoader",
    "Since 1.12.0, will remove in 1.14.0"
  )
  def classLoader = _classLoader

  /**
   * Set the file format. If set to null, try to guess from any available
   * filename extension; if guessing fails, assume [[ConfigSyntax#CONF]].
   *
   * @param syntax
   *   a syntax or `null` for best guess
   * @return
   *   options with the syntax set
   */
  def setSyntax(syntax: ConfigSyntax): ConfigParseOptions =
    if (_syntax == syntax) this
    else
      new ConfigParseOptions(
        syntax,
        _originDescription,
        _allowMissing,
        _includer,
        _classLoader,
        _keepDetachedComments
      )

  /**
   * Set the file format. If set to null, assume [[ConfigSyntax#CONF]].
   *
   * @param filename
   *   a configuration file name
   * @return
   *   options with the syntax set
   */
  def setSyntaxFromFilename(filename: String): ConfigParseOptions = {
    val syntax = ConfigImplUtil.syntaxFromExtension(filename)
    setSyntax(syntax)
  }

  /**
   * Gets the current syntax option, which may be null for "any".
   *
   * @return
   *   the current syntax or null
   */
  def getSyntax: ConfigSyntax = _syntax

  /**
   * Set a description for the thing being parsed. In most cases this will be
   * set up for you to something like the filename, but if you provide just an
   * input stream you might want to improve on it. Set to null to allow the
   * library to come up with something automatically. This description is the
   * basis for the [[ConfigOrigin]] of the parsed values.
   *
   * @param originDescription
   *   description to put in the [[ConfigOrigin]]
   * @return
   *   options with the origin description set
   */
  def setOriginDescription(originDescription: String): ConfigParseOptions = { // findbugs complains about == here but is wrong, do not "fix"
    if (_originDescription == originDescription)
      this
    else if (_originDescription != null && originDescription != null && _originDescription == originDescription)
      this
    else
      new ConfigParseOptions(
        _syntax,
        originDescription,
        _allowMissing,
        _includer,
        _classLoader,
        _keepDetachedComments
      )
  }

  /**
   * Gets the current origin description, which may be null for "automatic".
   *
   * @return
   *   the current origin description or null
   */
  def getOriginDescription: String = _originDescription

  /** this is package-private, not public API */
  private[config] def withFallbackOriginDescription(originDescription: String) =
    if (_originDescription == null)
      setOriginDescription(originDescription)
    else
      this

  /**
   * Set to false to throw an exception if the item being parsed (for example a
   * file) is missing. Set to true to just return an empty document in that
   * case. Note that this setting applies on only to fetching the root document,
   * it has no effect on any nested includes.
   *
   * @param allowMissing
   *   true to silently ignore missing item
   * @return
   *   options with the "allow missing" flag set
   */
  def setAllowMissing(allowMissing: Boolean): ConfigParseOptions =
    if (_allowMissing == allowMissing)
      this
    else
      new ConfigParseOptions(
        _syntax,
        _originDescription,
        allowMissing,
        _includer,
        _classLoader,
        _keepDetachedComments
      )

  /**
   * Gets the current "allow missing" flag.
   *
   * @return
   *   whether we allow missing files
   */
  def getAllowMissing: Boolean = _allowMissing

  /**
   * Set a [[ConfigIncluder]] which customizes how includes are handled. null
   * means to use the default includer.
   *
   * @param includer
   *   the includer to use or null for default
   * @return
   *   new version of the parse options with different includer
   */
  def setIncluder(includer: ConfigIncluder): ConfigParseOptions =
    if (_includer == includer)
      this
    else
      new ConfigParseOptions(
        _syntax,
        _originDescription,
        _allowMissing,
        includer,
        _classLoader,
        _keepDetachedComments
      )

  /**
   * Prepends a [[ConfigIncluder]] which customizes how includes are handled. To
   * prepend your includer, the library calls [[ConfigIncluder#withFallback]] on
   * your includer to append the existing includer to it.
   *
   * @param includer
   *   the includer to prepend (may not be null)
   * @return
   *   new version of the parse options with different includer
   */
  def prependIncluder(includer: ConfigIncluder): ConfigParseOptions = {
    if (includer == null)
      throw new NullPointerException("null includer passed to prependIncluder")
    if (_includer eq includer)
      this
    else if (_includer != null)
      setIncluder(includer.withFallback(_includer))
    else
      setIncluder(includer)
  }

  /**
   * Appends a [[ConfigIncluder]] which customizes how includes are handled. To
   * append, the library calls [[ConfigIncluder#withFallback]] on the existing
   * includer.
   *
   * @param includer
   *   the includer to append (may not be null)
   * @return
   *   new version of the parse options with different includer
   */
  def appendIncluder(includer: ConfigIncluder): ConfigParseOptions = {
    if (includer == null)
      throw new NullPointerException("null includer passed to appendIncluder")
    if (_includer == includer)
      this
    else if (_includer != null)
      setIncluder(_includer.withFallback(includer))
    else
      setIncluder(includer)
  }

  /**
   * Gets the current includer (will be null for the default includer).
   *
   * @return
   *   current includer or null
   */
  def getIncluder: ConfigIncluder = _includer

  /**
   * Set the class loader. If set to null,
   * `Thread.currentThread().getContextClassLoader()` will be used.
   *
   * @param loader
   *   a class loader or `null` to use thread context class loader
   * @return
   *   options with the class loader set
   */
  def setClassLoader(loader: ClassLoader): ConfigParseOptions =
    if (_classLoader == loader)
      this
    else
      new ConfigParseOptions(
        _syntax,
        _originDescription,
        _allowMissing,
        _includer,
        loader,
        _keepDetachedComments
      )

  /**
   * Get the class loader; never returns `null`, if the class loader was unset,
   * returns `Thread.currentThread().getContextClassLoader()`.
   *
   * @return
   *   class loader to use
   */
  def getClassLoader: ClassLoader =
    if (_classLoader == null) {
      val thread = Thread.currentThread
      new PlatformThread(thread).getContextClassLoader()
    } else {
      _classLoader
    }

  /**
   * Set to keep *detached* comments: a comment block separated from the next
   * object field or array element by a blank line. Off by default: the parser
   * then drops the block exactly as `lightbend/config` does. This is an
   * sconfig-only option, not in `lightbend/config`; see `docs/NEW_FEATURES.md`
   * for the examples.
   *
   * With the flag on:
   *
   *   - comment origins change: `ConfigOrigin.comments` gains the kept blocks,
   *     and a render with comments on prints them. The resolved values do not
   *     change in the tested cases.
   *   - order is kept within a block; across duplicate fields or elements the
   *     merge policy decides, so a block can still be replaced or merged.
   *   - a header outside a braced root is kept in the root origin but is not
   *     rendered.
   *   - a block before `include` attaches to the next local field, not to the
   *     included file.
   *   - a comment above a `+=` field is printed twice, above the delayed
   *     assignment and above its element: an existing `+=` defect that
   *     preservation exposes, pinned in `ConfigDefaultRenderingTest`.
   *   - with `ConfigFormatOptions.setSimplifyNestedObjects(true)`, an object
   *     that carries a kept comment is no longer compressed to a dotted path,
   *     even when comments are not rendered.
   *
   * @param value
   *   true to keep the comments, false to drop them as `lightbend/config` does
   * @return
   *   options with the flag set
   */
  def setKeepDetachedComments(value: Boolean): ConfigParseOptions =
    if (_keepDetachedComments == value) this
    else
      new ConfigParseOptions(
        _syntax,
        _originDescription,
        _allowMissing,
        _includer,
        _classLoader,
        value
      )

  /**
   * Gets the current "keep detached comments" flag.
   *
   * @return
   *   whether a detached comment block, one separated from the next field or
   *   element by a blank line, is kept
   */
  def getKeepDetachedComments: Boolean =
    _keepDetachedComments
}
