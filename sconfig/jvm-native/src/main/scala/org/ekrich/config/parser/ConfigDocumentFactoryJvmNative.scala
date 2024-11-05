package org.ekrich.config.parser

import java.io.File

import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.impl.Parseable

/**
 * [[ConfigDocumentFactory]] methods common to JVM and Native
 */
abstract class ConfigDocumentFactoryJvmNative
    extends ConfigDocumentFactoryShared {

  /**
   * Parses a file into a ConfigDocument instance.
   *
   * @param file
   *   the file to parse
   * @param options
   *   parse options to control how the file is interpreted
   * @return
   *   the parsed configuration
   * @throws org.ekrich.config.ConfigException
   *   on IO or parse errors
   */
  def parseFile(file: File, options: ConfigParseOptions): ConfigDocument =
    Parseable.newFile(file, options).parseConfigDocument()

  /**
   * Parses a file into a ConfigDocument instance as with
   * [[#parseFile(file:java\.io\.File,options:org\.ekrich\.config\.ConfigParseOptions)* parseFile(File, ConfigParseOptions)]]
   * but always uses the default parse options.
   *
   * @param file
   *   the file to parse
   * @return
   *   the parsed configuration
   * @throws org.ekrich.config.ConfigException
   *   on IO or parse errors
   */
  def parseFile(file: File): ConfigDocument =
    parseFile(file, ConfigParseOptions.defaults)

}
