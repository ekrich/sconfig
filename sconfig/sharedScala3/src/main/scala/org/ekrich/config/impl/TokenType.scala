/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{lang => jl}

enum TokenType extends jl.Enum[TokenType] {
  case START, END, COMMA, EQUALS, COLON, OPEN_CURLY, CLOSE_CURLY, OPEN_SQUARE, CLOSE_SQUARE, VALUE, NEWLINE, UNQUOTED_TEXT, IGNORED_WHITESPACE, SUBSTITUTION, PROBLEM, COMMENT, PLUS_EQUALS
}
