/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{util => ju}

/**
 * Interface that tags a ConfigValue that is not mergeable until after
 * substitutions are resolved. Basically these are special ConfigValue that
 * never appear in a resolved tree, like `ConfigSubstitution` and
 * [[ConfigDelayedMerge]].
 */
trait Unmergeable {
  def unmergedValues: ju.Collection[_ <: AbstractConfigValue]
}
