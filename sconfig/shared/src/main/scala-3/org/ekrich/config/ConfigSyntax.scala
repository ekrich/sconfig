/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import java.{lang => jl}

/**
 * The syntax of a character stream (<a href="http://json.org">JSON</a>, <a
 * href="https://github.com/lightbend/config/blob/master/HOCON.md">HOCON</a> aka
 * ".conf", or <a href=
 * "http://download.oracle.com/javase/7/docs/api/java/util/Properties.html#load%28java.io.Reader%29"
 * >Java properties</a>).
 */
enum ConfigSyntax extends jl.Enum[ConfigSyntax] {

  /**
   * Pedantically strict <a href="http://json.org">JSON</a> format; no comments,
   * no unexpected commas, no duplicate keys in the same object. Associated with
   * the `.json</code> file extension and <code>application/json`
   * Content-Type.
   */
  case JSON

  /**
   * The JSON-superset <a
   * href="https://github.com/lightbend/config/blob/master/HOCON.md" >HOCON</a>
   * format. Associated with the `.conf` file extension and
   * `application/hocon` Content-Type.
   */
  case CONF

  /**
   * Standard <a href=
   * "http://download.oracle.com/javase/7/docs/api/java/util/Properties.html#load%28java.io.Reader%29"
   * >Java properties</a> format. Associated with the `.properties`
   * file extension and `text/x-java-properties` Content-Type.
   */
  case PROPERTIES
}
