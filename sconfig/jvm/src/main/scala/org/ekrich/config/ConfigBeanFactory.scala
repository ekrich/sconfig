package org.ekrich.config

import org.ekrich.config.impl.ConfigBeanImpl

/**
 * Factory for automatically creating a Java class from a [[Config]].
 * See [[ConfigBeanFactory.create(Config,Class)]].
 *
 * @since 1.3.0
 */
object ConfigBeanFactory {

  /**
   * Creates an instance of a class, initializing its fields from a [[Config]].
   *
   * Example usage:
   *
   * {{{
   * val configSource: Config = ConfigFactory.load().getConfig("foo");
   * val config: FooConfig = ConfigBeanFactory.create(configSource, classOf[FooConfig]);
   * }}}
   *
   * The Java class should follow JavaBean conventions. Field types
   * can be any of the types you can normally get from a [[Config]],
   * including `java.time.Duration` or [[ConfigMemorySize]].
   * Fields may also be another JavaBean-style
   * class.
   *
   * Fields are mapped to config by converting the config key to
   * camel case.  So the key `foo-bar` becomes JavaBean
   * setter `setFooBar`.
   *
   * @since 1.3.0
   * @param config source of config information
   * @param clazz class to be instantiated
   * @param <T> the type of the class to be instantiated
   * @return an instance of the class populated with data from the config
   * @throws [[ConfigException#BadBean]]
   *     If something is wrong with the JavaBean
   * @throws [[ConfigException#ValidationFailed]]
   *     If the config doesn't conform to the bean's implied schema
   * @throws ConfigException
   *     Can throw the same exceptions as the getters on `Config`
   */
  def create[T](config: Config, clazz: Class[T]): T =
    ConfigBeanImpl.createInternal(config, clazz)
}
