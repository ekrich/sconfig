/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{lang => jl}
import java.{util => ju}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigList
import org.ekrich.config.ConfigMergeable
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigRenderOptions
import org.ekrich.config.ConfigValue

// This is just like ConfigDelayedMerge except we know statically
// that it will turn out to be an object.
object ConfigDelayedMergeObject {
  private def notResolved =
    new ConfigException.NotResolved(
      "need to Config#resolve() before using this object, see the API docs for Config#resolve()"
    )
}

final class ConfigDelayedMergeObject(
    _origin: ConfigOrigin,
    val stack: ju.List[AbstractConfigValue]
) extends AbstractConfigObject(_origin)
    with Unmergeable
    with ReplaceableMergeStack {
  if (stack.isEmpty)
    throw new ConfigException.BugOrBroken("creating empty delayed merge object")
  if (!stack.get(0).isInstanceOf[AbstractConfigObject])
    throw new ConfigException.BugOrBroken(
      "created a delayed merge object not guaranteed to be an object"
    )
  stack.forEach { v =>
    v match {
      case _: ConfigDelayedMerge | _: ConfigDelayedMergeObject =>
        throw new ConfigException.BugOrBroken(
          "placed nested DelayedMerge in a ConfigDelayedMergeObject, should have consolidated stack"
        )
      case _ => ()
    }
  }

  override def newCopy(
      status: ResolveStatus,
      origin: ConfigOrigin
  ): ConfigDelayedMergeObject = {
    if (status ne resolveStatus)
      throw new ConfigException.BugOrBroken(
        "attempt to create resolved ConfigDelayedMergeObject"
      )
    new ConfigDelayedMergeObject(origin, stack)
  }
  @throws[AbstractConfigValue.NotPossibleToResolve]
  override def resolveSubstitutions(
      context: ResolveContext,
      source: ResolveSource
  ): ResolveResult[_ <: AbstractConfigObject] = {
    val merged =
      ConfigDelayedMerge.resolveSubstitutions(this, stack, context, source)
    merged.asObjectResult
  }
  override def makeReplacement(
      context: ResolveContext,
      skipping: Int
  ): AbstractConfigValue =
    ConfigDelayedMerge.makeReplacement(context, stack, skipping)
  override def resolveStatus: ResolveStatus = ResolveStatus.UNRESOLVED
  override def replaceChild(
      child: AbstractConfigValue,
      replacement: AbstractConfigValue
  ): AbstractConfigValue = {
    val newStack =
      AbstractConfigValue.replaceChildInList(stack, child, replacement)
    if (newStack == null) null
    else new ConfigDelayedMergeObject(origin, newStack)
  }
  override def hasDescendant(descendant: AbstractConfigValue): Boolean =
    AbstractConfigValue.hasDescendantInList(stack, descendant)
  override def relativized(prefix: Path): ConfigDelayedMergeObject = {
    val newStack = new ju.ArrayList[AbstractConfigValue]
    stack.forEach { o =>
      newStack.add(o.relativized(prefix))
    }
    new ConfigDelayedMergeObject(origin, newStack)
  }
  override def ignoresFallbacks: Boolean =
    ConfigDelayedMerge.stackIgnoresFallbacks(stack)
  override final def mergedWithTheUnmergeable(
      fallback: Unmergeable
  ): ConfigDelayedMergeObject = {
    requireNotIgnoringFallbacks()
    mergedWithTheUnmergeable(stack, fallback)
      .asInstanceOf[ConfigDelayedMergeObject]
  }
  override final def mergedWithObject(
      fallback: AbstractConfigObject
  ): ConfigDelayedMergeObject =
    mergedWithNonObject(fallback)
  override final def mergedWithNonObject(
      fallback: AbstractConfigValue
  ): ConfigDelayedMergeObject = {
    requireNotIgnoringFallbacks()
    mergedWithNonObject(stack, fallback)
      .asInstanceOf[ConfigDelayedMergeObject]
  }
  override def withFallback(
      mergeable: ConfigMergeable
  ): ConfigDelayedMergeObject =
    super.withFallback(mergeable).asInstanceOf[ConfigDelayedMergeObject]
  override def withOnlyKey(key: String): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withoutKey(key: String): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withOnlyPathOrNull(path: Path): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withOnlyPath(path: Path): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withoutPath(path: Path): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withValue(
      key: String,
      value: ConfigValue
  ): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def withValue(path: Path, value: ConfigValue): AbstractConfigObject =
    throw ConfigDelayedMergeObject.notResolved
  override def unmergedValues: ju.Collection[AbstractConfigValue] = stack
  override def canEqual(other: Any): Boolean =
    other.isInstanceOf[ConfigDelayedMergeObject]
  override def equals(other: Any): Boolean = { // note that "origin" is deliberately NOT part of equality
    if (other.isInstanceOf[ConfigDelayedMergeObject])
      canEqual(other) && ((this.stack eq other
        .asInstanceOf[ConfigDelayedMergeObject]
        .stack) || this.stack == other
        .asInstanceOf[ConfigDelayedMergeObject]
        .stack)
    else false
  }
  override def hashCode: Int = stack.hashCode
  override def render(
      sb: jl.StringBuilder,
      indent: Int,
      atRoot: Boolean,
      atKey: String,
      options: ConfigRenderOptions
  ): Unit = {
    ConfigDelayedMerge.render(stack, sb, indent, atRoot, atKey, options)
  }
  override def renderValue(
      sb: jl.StringBuilder,
      indent: Int,
      atRoot: Boolean,
      options: ConfigRenderOptions
  ): Unit = {
    render(sb, indent, atRoot, null, options)
  }
  override def unwrapped: ju.Map[String, AnyRef] =
    throw ConfigDelayedMergeObject.notResolved
  override def get(key: Any): AbstractConfigValue =
    throw ConfigDelayedMergeObject.notResolved
  override def remove(key: Any): ConfigValue =
    throw ConfigDelayedMergeObject.notResolved
  override def containsKey(key: Any): Boolean =
    throw ConfigDelayedMergeObject.notResolved
  override def containsValue(value: Any): Boolean =
    throw ConfigDelayedMergeObject.notResolved
  override def entrySet: ju.Set[ju.Map.Entry[String, ConfigValue]] =
    throw ConfigDelayedMergeObject.notResolved
  override def isEmpty: Boolean = throw ConfigDelayedMergeObject.notResolved
  override def keySet: ju.Set[String] =
    throw ConfigDelayedMergeObject.notResolved
  override def size: Int = throw ConfigDelayedMergeObject.notResolved
  override def values: ju.Collection[ConfigValue] =
    throw ConfigDelayedMergeObject.notResolved

  // exercised in ValidationTest.validationFailedSerializable
  // and ConfigTest.test01Serializable
  override def attemptPeekWithPartialResolve(
      key: String
  ): AbstractConfigValue = {
    // a partial resolve of a ConfigDelayedMergeObject always results in a
    // SimpleConfigObject because all the substitutions in the stack get
    // resolved in order to look up the partial.
    // So we know here that we have not been resolved at all even
    // partially.
    // Given that, all this code is probably gratuitous, since the app code
    // is likely broken. But in general we only throw NotResolved if you try
    // to touch the exact key that isn't resolved, so this is in that
    // spirit.
    // we'll be able to return a key if we have a value that ignores
    // fallbacks, prior to any unmergeable values.
    @tailrec
    def loop(
        layers: List[AbstractConfigValue]
    ): Either[ConfigException, AbstractConfigValue] =
      layers match {
        case Nil =>
          // If we get here, then we never found anything unresolved which means
          // the ConfigDelayedMergeObject should not have existed. some
          // invariant was violated.
          Left(
            new ConfigException.BugOrBroken(
              "Delayed merge stack does not contain any unmergeable values"
            )
          )
        case head :: tl =>
          head match {
            case layer: AbstractConfigObject => {
              layer.attemptPeekWithPartialResolve(key) match {
                case v if v != null =>
                  if (v.ignoresFallbacks) {
                    // we know we won't need to merge anything in to this value
                    Right(v)
                  } else {
                    // we can't return this value because we know there are
                    // unmergeable values later in the stack that may
                    // contain values that need to be merged with this
                    // value. we'll throw the exception when we get to those
                    // unmergeable values, so continue here.
                    loop(tl)
                  }
                case _: Unmergeable =>
                  // an unmergeable object (which would be another
                  // ConfigDelayedMergeObject) can't know that a key is
                  // missing, so it can't return null; it can only return a
                  // value or throw NotPossibleToResolve
                  throw new ConfigException.BugOrBroken(
                    "should not be reached: unmergeable object returned null value"
                  )
                case _ =>
                  // a non-unmergeable AbstractConfigObject that returned null
                  // for the key in question is not relevant, we can keep
                  // looking for a value.
                  loop(tl)
              }
            }
            case _: Unmergeable =>
              throw new ConfigException.NotResolved(
                s"Key '$key' is not available at '${origin.description}' because value at '${head.origin.description}'" +
                  s" has not been resolved and may turn out to contain or hide '$key'." +
                  " Be sure to Config#resolve() before using a config object."
              )
            case layer if (layer.resolveStatus eq ResolveStatus.UNRESOLVED) =>
              // if the layer is not an object, and not a substitution or merge,
              // then it's something that's unresolved because it _contains_
              // an unresolved object... i.e. it's an array
              layer match {
                case _: ConfigList =>
                  // all later objects will be hidden so we can say we won't find
                  // the key
                  Right(null)
                case _ =>
                  throw new ConfigException.BugOrBroken(
                    "Expecting a list here, not " + layer
                  )
              }

            case _ =>
              // non-object, but resolved, like an integer or something.
              // has no children so the one we're after won't be in it.
              // we would only have this in the stack in case something
              // else "looks back" to it due to a cycle.
              // anyway at this point we know we can't find the key anymore.
              if (head.ignoresFallbacks)
                Right(null)
              else {
                throw new ConfigException.BugOrBroken(
                  "resolved non-object should ignore fallbacks"
                )
              }
          }
      }
    // run the logic
    loop(stack.asScala.toList) match {
      case Right(res) =>
        res
      case Left(ex) =>
        throw ex
    }
  }
}
