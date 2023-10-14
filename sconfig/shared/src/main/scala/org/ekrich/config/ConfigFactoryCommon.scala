package org.ekrich.config

import org.ekrich.config.impl.Parseable

abstract class ConfigFactoryCommon {

  /**
   * Parses a string (which should be valid HOCON or JSON by default, or the
   * syntax specified in the options otherwise).
   *
   * @param s
   *   string to parse
   * @param options
   *   parse options
   * @return
   *   the parsed configuration
   */
  def parseString(s: String, options: ConfigParseOptions): Config =
    Parseable.newString(s, options).parse().toConfig

  /**
   * Parses a string (which should be valid HOCON or JSON).
   *
   * @param s
   *   string to parse
   * @return
   *   the parsed configuration
   */
  def parseString(s: String): Config =
    parseString(s, ConfigParseOptions.defaults)

}
