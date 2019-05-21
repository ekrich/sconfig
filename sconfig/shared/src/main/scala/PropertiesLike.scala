package org.ekrich.config.impl

import java.io.Reader
import java.util.Properties

/**
 * To workaround missing implementation in Scala.js
 * Can be removed once implemented in Scala.js
 */
trait PropertiesLike {
  def load(reader: Reader): Unit
}
