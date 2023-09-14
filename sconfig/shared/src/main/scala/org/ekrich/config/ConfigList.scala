/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import java.{util => ju}

/**
 * Subtype of [[ConfigValue]] representing a list value, as in JSON's `[1,2,3]`
 * syntax.
 *
 * <p> `ConfigList` implements `java.util.List[ConfigValue]` so you can use it
 * like a regular Java list. Or call [[#unwrapped]] to unwrap the list elements
 * into plain Java values.
 *
 * <p> Like all [[ConfigValue]] subtypes, `ConfigList` is immutable. This makes
 * it threadsafe and you never have to create "defensive copies." The mutator
 * methods from `java.util.List` all throw
 * `java.lang.UnsupportedOperationException`.
 *
 * <p> The [[ConfigValue#valueType]] method on a list returns
 * [[ConfigValueType#LIST]].
 *
 * <p> <em>Do not implement `ConfigList`</em>; it should only be implemented by
 * the config library. Arbitrary implementations will not work because the
 * library internals assume a specific concrete implementation. Also, this
 * interface is likely to grow new methods over time, so third-party
 * implementations will break.
 */
trait ConfigList extends ju.List[ConfigValue] with ConfigValue {

  /**
   * Recursively unwraps the list, returning a list of plain Java values such as
   * Integer or String or whatever is in the list.
   *
   * @return
   *   a `java.util.List` containing plain Java objects
   */
  override def unwrapped: ju.List[AnyRef]
  override def withOrigin(origin: ConfigOrigin): ConfigList
}
