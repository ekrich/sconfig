package org.ekrich.config

object ConfigFormatOptions {
  def defaults = new ConfigFormatOptions(false, true, false, true, false)
}

final class ConfigFormatOptions private (
    keepOriginOrder: Boolean,
    doubleIndent: Boolean,
    colonAssign: Boolean,
    newLineAtEnd: Boolean,
    simplifyOneEntryNestedObjects: Boolean
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
