package org.ekrich.config

import java.io.Reader

import org.ekrich.config.impl.Parseable

/**
 * [[ConfigFactory]] methods shared by all platforms
 */
abstract class ConfigFactoryShared {

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

  /**
   * Parses a Reader into a Config instance. Does not call
   * [[Config!.resolve()* Config.resolve()]] or merge the parsed stream with any
   * other configuration; this method parses a single stream and does nothing
   * else. It does process "include" statements in the parsed stream, and may
   * end up doing other IO due to those statements.
   *
   * @param reader
   *   the reader to parse
   * @param options
   *   parse options to control how the reader is interpreted
   * @return
   *   the parsed configuration
   * @throws ConfigException
   *   on IO or parse errors
   */
  def parseReader(reader: Reader, options: ConfigParseOptions): Config =
    Parseable.newReader(reader, options).parse().toConfig

  /**
   * Parses a reader into a Config instance as with
   * [[#parseReader(reader:java\.io\.Reader,options:org\.ekrich\.config\.ConfigParseOptions)* parseReader(Reader, ConfigParseOptions)]]
   * but always uses the default parse options.
   *
   * @param reader
   *   the reader to parse
   * @return
   *   the parsed configuration
   * @throws ConfigException
   *   on IO or parse errors
   */
  def parseReader(reader: Reader): Config =
    parseReader(reader, ConfigParseOptions.defaults)

}
