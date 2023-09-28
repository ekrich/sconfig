package org.ekrich.config.impl

import java.net.URL

/**
 * To workaround missing implementations in Scala.js and Scala Native
 */
trait TraitUri {
  def toURL(): URL
}
