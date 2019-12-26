/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

/**
 * Marker for types whose instances can be merged, that is [[Config]] and
 * [[ConfigValue]]. Instances of `Config` and `ConfigValue` can
 * be combined into a single new instance using the
 * [[ConfigMergeable#withFallback withFallback()]] method.
 *
 * ''Do not implement this interface''; it should only be implemented by
 * the config library. Arbitrary implementations will not work because the
 * library internals assume a specific concrete implementation. Also, this
 * interface is likely to grow new methods over time, so third-party
 * implementations will break.
 */
trait ConfigMergeable {

  /**
   * Returns a new value computed by merging this value with another, with
   * keys in this value "winning" over the other one.
   *
   * This associative operation may be used to combine configurations from
   * multiple sources (such as multiple configuration files).
   *
   * The semantics of merging are described in the
   * [[https://github.com/ekrich/sconfig/blob/master/HOCON.md spec for HOCON]].
   * Merging typically occurs when either the same object is
   * created twice in the same file, or two config files are both loaded. For
   * example:
   *
   * {{{
   *   foo = { a: 42 }
   *   foo = { b: 43 }
   * }}}
   *
   * Here, the two objects are merged as if you had written:
   *
   * {{{
   *   foo = { a: 42, b: 43 }
   * }}}
   *
   * Only {@link ConfigObject} and {@link Config} instances do anything in
   * this method (they need to merge the fallback keys into themselves). All
   * other values just return the original value, since they automatically
   * override any fallback. This means that objects do not merge "across"
   * non-objects; if you write
   * `object.withFallback(nonObject).withFallback(otherObject)`,
   * then `otherObject`will simply be ignored. This is an
   * intentional part of how merging works, because non-objects such as
   * strings and integers replace (rather than merging with) any prior value:
   *
   * {{{
   *   foo = { a: 42 }
   *   foo = 10
   * }}}
   *
   * Here, the number 10 "wins" and the value of `foo` would be
   * simply 10. Again, for details see the spec.
   *
   * @param other
   *            an object whose keys should be used as fallbacks, if the keys
   *            are not present in this one
   * @return a new object (or the original one, if the fallback doesn't get
   *         used)
   */
  def withFallback(other: ConfigMergeable): ConfigMergeable
}
