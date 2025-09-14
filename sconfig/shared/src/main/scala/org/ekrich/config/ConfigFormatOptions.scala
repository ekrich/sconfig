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
    private val simplifyOneEntryNestedObjects: Boolean
) {
  def setKeepOriginOrder(value: Boolean): ConfigFormatOptions =
    if (value == keepOriginOrder) this
    else
      new ConfigFormatOptions(
        value,
        doubleIndent,
        colonAssign,
        newLineAtEnd,
        simplifyOneEntryNestedObjects
      )

  def getKeepOriginOrder: Boolean = keepOriginOrder

  def setDoubleIndent(value: Boolean): ConfigFormatOptions =
    if (value == doubleIndent) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        value,
        colonAssign,
        newLineAtEnd,
        simplifyOneEntryNestedObjects
      )

  def getDoubleIndent: Boolean = doubleIndent

  def setColonAssign(value: Boolean): ConfigFormatOptions =
    if (value == colonAssign) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        value,
        newLineAtEnd,
        simplifyOneEntryNestedObjects
      )

  def getColonAssign: Boolean = colonAssign

  def setNewLineAtEnd(value: Boolean): ConfigFormatOptions =
    if (value == newLineAtEnd) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        colonAssign,
        value,
        simplifyOneEntryNestedObjects
      )

  def getNewLineAtEnd: Boolean = newLineAtEnd

  def setSimplifyOneEntryNestedObjects(value: Boolean): ConfigFormatOptions =
    if (value == simplifyOneEntryNestedObjects) this
    else
      new ConfigFormatOptions(
        keepOriginOrder,
        doubleIndent,
        colonAssign,
        newLineAtEnd,
        value
      )

  def getSimplifyOneEntryNestedObjects: Boolean = simplifyOneEntryNestedObjects

}
