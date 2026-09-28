package org.ekrich.config

/**
 * Default config loading strategy. Able to load resource, file or URL. Behavior
 * may be altered by defining one of VM properties `config.resource`,
 * `config.file` or `config.url`
 */
class DefaultConfigLoadingStrategy extends ConfigLoadingStrategy {
  override def parseApplicationConfig(
      parseOptions: ConfigParseOptions
  ): Config =
    ConfigFactory
      .parseApplicationReplacement(parseOptions)
      .orElseGet(() =>
        ConfigFactory.parseResourcesAnySyntax("application", parseOptions)
      )
}
