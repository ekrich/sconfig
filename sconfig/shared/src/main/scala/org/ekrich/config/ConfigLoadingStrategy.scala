package org.ekrich.config

/**
 * This method allows you to alter default config loading strategy for all the code which
 * calls one of the methods, e.g.
 * [[ConfigFactory$.load(resourceBasename:String)* ConfigFactory.load(String)]]
 *
 * Usually you don't have to implement this interface but it may be required
 * when you fixing a improperly implemented library with unavailable source code.
 *
 * You have to define VM property `config.strategy` to replace default strategy with your own.
 */
trait ConfigLoadingStrategy {
  /**
   * This method must load and parse application config.
   *
   * @param parseOptions [[ConfigParseOptions]] to use
   * @return loaded config
   */
  def parseApplicationConfig(parseOptions: ConfigParseOptions): Config
}
