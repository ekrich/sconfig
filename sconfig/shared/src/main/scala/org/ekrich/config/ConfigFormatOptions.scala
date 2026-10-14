package org.ekrich.config

/**
 * A set of options related to formatting.
 *
 * This object is immutable, so the "setters" return a new object.
 *
 * Here is an example of creating a custom `ConfigFormatOptions` and change two
 * defaults:
 *
 * {{{
 * val formatOptions = ConfigFormatOptions.defaults
 *   .setKeepOriginOrder(true)
 *   .setColonAssign(true)
 * }}}
 *
 * These options are for `ConfigDocument.format`. They are also carried on
 * [[ConfigRenderOptions]], with `doubleIndent = true`, but that use is
 * deprecated and will be removed in 3.0.0.
 *
 * @since 1.12.0
 */
object ConfigFormatOptions {

  /**
   * Create a `ConfigFormatOptions` object with the default values.
   *
   * @return
   *   the default options
   *
   *   - keepOriginOrder = false
   *   - doubleIndent = false
   *   - colonAssign = false
   *   - newLineAtEnd = true
   *   - simplifyOneEntryNestedObjects = false
   */
  def defaults = new ConfigFormatOptions(false, false, false, true, false)
}

final class ConfigFormatOptions private (
    private[config] val keepOriginOrder: Boolean,
    private val doubleIndent: Boolean,
    private val colonAssign: Boolean,
    private[config] val newLineAtEnd: Boolean,
    private val simplifyNestedObjects: Boolean
) {

  /**
   * Set to keep the origin order
   *
   * @param value
   *   true to enable the property, false otherwise
   * @return
   *   the new [[ConfigFormatOptions]] object
   */
  @deprecated(
    "Has no effect on ConfigDocument.format; only the deprecated ConfigRenderOptions formatting uses it",
    "Since 2.2.0, will remove in 3.0.0"
  )
  def setKeepOriginOrder(value: Boolean): ConfigFormatOptions =
    if (value == keepOriginOrder) this
    else
      new ConfigFormatOptions(
        value,
        doubleIndent,
        colonAssign,
        newLineAtEnd,
        simplifyNestedObjects
      )

  /**
   * Get the current formatting option value
   *
   * @return
   *   true if set, false otherwise
   */
  @deprecated(
    "Has no effect on ConfigDocument.format; only the deprecated ConfigRenderOptions formatting uses it",
    "Since 2.2.0, will remove in 3.0.0"
  )
  def getKeepOriginOrder: Boolean = keepOriginOrder

  /**
   * Set to enable double the indent (4 spaces vs 2)
   *
   * @param value
   *   true to enable the property, false otherwise
   * @return
   *   the new [[ConfigFormatOptions]] object
   */
  def setDoubleIndent(value: Boolean): ConfigFormatOptions =
    if (value == doubleIndent) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        value,
        colonAssign,
        newLineAtEnd,
        simplifyNestedObjects
      )

  /**
   * Get the current formatting option value
   *
   * @return
   *   true if set, false otherwise
   */
  def getDoubleIndent: Boolean = doubleIndent

  /**
   * Set to have properties use colons between the name and the value
   *
   * @param value
   *   true to enable the property, false otherwise
   * @return
   *   the new [[ConfigFormatOptions]] object
   */
  def setColonAssign(value: Boolean): ConfigFormatOptions =
    if (value == colonAssign) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        value,
        newLineAtEnd,
        simplifyNestedObjects
      )

  /**
   * Get the current formatting option value
   *
   * @return
   *   true if set, false otherwise
   */
  def getColonAssign: Boolean = colonAssign

  /**
   * Set to have a new line at the end of the file
   *
   * @param value
   *   true to enable the property, false otherwise
   * @return
   *   the new [[ConfigFormatOptions]] object
   */
  @deprecated(
    "Has no effect on ConfigDocument.format; only the deprecated ConfigRenderOptions formatting uses it",
    "Since 2.2.0, will remove in 3.0.0"
  )
  def setNewLineAtEnd(value: Boolean): ConfigFormatOptions =
    if (value == newLineAtEnd) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        colonAssign,
        value,
        simplifyNestedObjects
      )

  /**
   * Get the current formatting option value
   *
   * @return
   *   true if set, false otherwise
   */
  @deprecated(
    "Has no effect on ConfigDocument.format; only the deprecated ConfigRenderOptions formatting uses it",
    "Since 2.2.0, will remove in 3.0.0"
  )
  def getNewLineAtEnd: Boolean = newLineAtEnd

  /**
   * Set to simplify nested objects
   *
   * @param value
   *   true to enable the property, false otherwise
   * @return
   *   the new [[ConfigFormatOptions]] object
   */
  def setSimplifyNestedObjects(value: Boolean): ConfigFormatOptions =
    if (value == simplifyNestedObjects) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        colonAssign,
        newLineAtEnd,
        value
      )

  /**
   * Get the current formatting option value
   *
   * @return
   *   true if set, false otherwise
   */
  def getSimplifyNestedObjects: Boolean = simplifyNestedObjects

}
