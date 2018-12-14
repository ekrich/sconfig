/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

/**
 * The type of a configuration value (following the
 * [[http://json.org JSON]] type schema).
 */
final class ConfigValueType private (name: String, ordinal: Int)
    extends Enum[ConfigValueType](name, ordinal)

object ConfigValueType {

  final val OBJECT  = new ConfigValueType("OBJECT", 0)
  final val LIST    = new ConfigValueType("LIST", 1)
  final val NUMBER  = new ConfigValueType("NUMBER", 2)
  final val BOOLEAN = new ConfigValueType("BOOLEAN", 3)
  final val NULL    = new ConfigValueType("NULL", 4)
  final val STRING  = new ConfigValueType("STRING", 5)

  private[this] final val _values: Array[ConfigValueType] =
    Array(OBJECT, LIST, NUMBER, BOOLEAN, NULL, STRING)

  def values: Array[ConfigValueType] = _values.clone()

  def valueOf(name: String): ConfigValueType =
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum const ConfigValueType." + name)
    }
}
