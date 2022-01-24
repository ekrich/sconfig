/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigOrigin

object Token { // this is used for singleton tokens like COMMA or OPEN_CURLY
  def newWithoutOrigin(
      tokenType: TokenType,
      debugString: String,
      tokenText: String
  ) =
    new Token(tokenType, null, tokenText, debugString)
}

class Token private[impl] (
    _tokenType: TokenType,
    _origin: ConfigOrigin,
    _tokenText: String,
    debugString: String
) {
  def this(_tokenType: TokenType, _origin: ConfigOrigin, _tokenText: String) =
    this(_tokenType, _origin, _tokenText, null)

  def this(_tokenType: TokenType, _origin: ConfigOrigin) =
    this(_tokenType, _origin, null)

  final private[impl] def tokenType: TokenType = _tokenType

  def tokenText: String = _tokenText

  // this is final because we don't always use the origin() accessor,
  // and we don't because it throws if origin is null
  final private[impl] def origin: ConfigOrigin = {
    // code is only supposed to call origin() on token types that
    // are expected to have an origin.
    if (_origin == null)
      throw new ConfigException.BugOrBroken(
        "tried to get origin from token that doesn't have one: " + this
      )
    _origin
  }

  final private[impl] def lineNumber =
    if (_origin != null) origin.lineNumber else -1

  override def toString(): String =
    if (debugString != null) debugString else tokenType.name

  protected def canEqual(other: Any): Boolean = other.isInstanceOf[Token]

  override def equals(other: Any): Boolean =
    if (other.isInstanceOf[Token]) { // origin is deliberately left out
      canEqual(other) && (this.tokenType eq other
        .asInstanceOf[Token]
        .tokenType)
    } else false

  override def hashCode(): Int = tokenType.hashCode
}
