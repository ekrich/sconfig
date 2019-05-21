package org.ekrich.config.impl

import java.util.Properties
import java.io.Reader

/**
 * To workaround missing implementation in Scala.js
 */
class PlatformProperties(props: Properties) extends PropertiesLike {
  def load(reader: Reader): Unit = props.load(reader)
}
