/**
 * Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.ekrich.config.ConfigException
import java.{util => ju}

final class ConfigNodeSimpleValue private[impl] (val token: Token)
    extends AbstractConfigNodeValue {
  override def tokens: ju.Collection[Token] =
    ju.Collections.singletonList(token)

  private[impl] def value: AbstractConfigValue = {
    if (Tokens.isValue(token)) return Tokens.getValue(token)
    else if (Tokens.isUnquotedText(token))
      return new ConfigString.Unquoted(
        token.origin,
        Tokens.getUnquotedText(token)
      )
    else if (Tokens.isSubstitution(token)) {
      val expression =
        Tokens.getSubstitutionPathExpression(token)
      val optional = Tokens.getSubstitutionOptional(token)

      // Detect and strip a trailing [] for the list-expansion-from-env-var
      // syntax. Inside a substitution, [] is tokenized as OPEN_SQUARE +
      // CLOSE_SQUARE like anywhere else; the parser sorts out that it's
      // only valid as the very last two tokens of the expression.
      val size = expression.size
      val (pathExpression, listExpansion) =
        if (size >= 2 &&
            (expression.get(size - 2) eq Tokens.OPEN_SQUARE) &&
            (expression.get(size - 1) eq Tokens.CLOSE_SQUARE))
          (expression.subList(0, size - 2), true)
        else (expression, false)

      val path =
        PathParser.parsePathExpression(pathExpression.iterator, token.origin)
      return new ConfigReference(
        token.origin,
        new SubstitutionExpression(path, optional, listExpansion)
      )
    }
    throw new ConfigException.BugOrBroken(
      "ConfigNodeSimpleValue did not contain a valid value token"
    )
  }
}
