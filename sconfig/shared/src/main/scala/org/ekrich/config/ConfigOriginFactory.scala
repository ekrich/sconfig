package org.ekrich.config

import java.net.URL
import org.ekrich.config.impl.ConfigImpl

/**
 * This class contains some static factory methods for building a [[ConfigOrigin]].
 * [[ConfigOrigin]]s are automatically created when you
 * call other API methods to get a [[ConfigValue]] or [[Config]].
 * But you can also set the origin of an existing [[ConfigValue]], using
 * [[ConfigValue.withOrigin(ConfigOrigin)]].
 *
 * @since 1.3.0
 */
object ConfigOriginFactory {
  /**
   * Returns the default origin for values when no other information is
   * provided. This is the origin used in
   * [[ConfigValueFactory.fromAnyRef(Object)]].
   *
   * @since 1.3.0
   * @return the default origin
   */
  def newSimple(): ConfigOrigin = newSimple(null)

  /**
   * Returns an origin with the given description.
   *
   * @since 1.3.0
   * @param description brief description of what the origin is
   * @return a new origin
   */
  def newSimple(description: String): ConfigOrigin =
    ConfigImpl.newSimpleOrigin(description)

  /**
   * Creates a file origin with the given filename.
   *
   * @since 1.3.0
   * @param filename the filename of this origin
   * @return a new origin
   */
  def newFile(filename: String): ConfigOrigin =
    ConfigImpl.newFileOrigin(filename)

  /**
   * Creates a url origin with the given URL object.
   *
   * @since 1.3.0
   * @param url the url of this origin
   * @return a new origin
   */
  def newURL(url: URL): ConfigOrigin = ConfigImpl.newURLOrigin(url)
}

final class ConfigOriginFactory private () {}
