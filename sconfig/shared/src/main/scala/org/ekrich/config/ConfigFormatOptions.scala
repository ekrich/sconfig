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
 * val options = ConfigFormatOptions.defaults()
 *   .setKeepOriginOrder(true)
 *   .setDoubleIndent(false)
 * }}}
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
   *   - doubleIndent = true
   *   - colonAssign = false
   *   - newLineAtEnd = true
   *   - simplifyOneEntryNestedObjects = false
   */
  def defaults = new ConfigFormatOptions(false, true, false, true, false)
}

final class ConfigFormatOptions private (
    private val keepOriginOrder: Boolean,
    private val doubleIndent: Boolean,
    private val colonAssign: Boolean,
    private val newLineAtEnd: Boolean,
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
