package org.ekrich.config.impl

import org.ekrich.config.ConfigMergeable
import org.ekrich.config.ConfigValue

trait MergeableValue extends ConfigMergeable { // converts a Config to its root object and a ConfigValue to itself
  def toFallbackValue(): ConfigValue
}
