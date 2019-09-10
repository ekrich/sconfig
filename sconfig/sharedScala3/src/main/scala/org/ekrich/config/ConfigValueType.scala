/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import java.{lang => jl}

/**
 * The type of a configuration value (following the
 * [[http://json.org JSON]] type schema).
 */
enum ConfigValueType extends jl.Enum[ConfigValueType] {
  case OBJECT, LIST, NUMBER, BOOLEAN, NULL, STRING
}
