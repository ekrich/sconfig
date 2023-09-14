/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

/**
 * Implement this interface and provide an instance to
 * [[ConfigParseOptions#setIncluder ConfigParseOptions.setIncluder()]] to
 * customize handling of {@code include} statements in config files. You may
 * also want to implement [[ConfigIncluderClasspath]],
 * [[ConfigIncluderFile]], and [[ConfigIncluderURL]], or not.
 */
trait ConfigIncluder {

  /**
   * Returns a new includer that falls back to the given includer. This is how
   * you can obtain the default includer; it will be provided as a fallback.
   * It's up to your includer to chain to it if you want to. You might want to
   * merge any files found by the fallback includer with any objects you load
   * yourself.
   *
   * It's important to handle the case where you already have the fallback with
   * a "return this", i.e. this method should not create a new object if the
   * fallback is the same one you already have. The same fallback may be added
   * repeatedly.
   *
   * @param fallback
   *   the previous includer for chaining
   * @return
   *   a new includer
   */
  def withFallback(fallback: ConfigIncluder): ConfigIncluder

  /**
   * Parses another item to be included. The returned object typically would not
   * have substitutions resolved. You can throw a ConfigException here to abort
   * parsing, or return an empty object, but may not return null.
   *
   * This method is used for a "heuristic" include statement that does not
   * specify file, URL, or classpath resource. If the include statement does
   * specify, then the same class implementing [[ConfigIncluder]] must also
   * implement [[ConfigIncluderClasspath}, {@link ConfigIncluderFile]], or
   * [[ConfigIncluderURL]] as needed, or a default includer will be used.
   *
   * @param context
   *   some info about the include context
   * @param what
   *   the include statement's argument
   * @return
   *   a non-null ConfigObject
   */
  def include(context: ConfigIncludeContext, what: String): ConfigObject
}
