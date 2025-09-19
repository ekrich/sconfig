/**
 * Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.ObjectStreamException
import java.io.Serializable
import java.util as ju
import java.lang as jl
import java.math.BigInteger
import scala.jdk.CollectionConverters.*
import scala.util.control.Breaks.*
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigRenderOptions
import org.ekrich.config.ConfigValue
import org.ekrich.config.impl.AbstractConfigValue.NotPossibleToResolve

@SerialVersionUID(2L)
object SimpleConfigObject {
  final private[impl] class ResolveModifier private[impl] (
      var context: ResolveContext,
      val source: ResolveSource
  ) extends AbstractConfigValue.Modifier {
    final private[impl] var originalRestrict = context.restrictToChild

    @throws[NotPossibleToResolve]
    override def modifyChildMayThrow(
        key: String,
        v: AbstractConfigValue
    ): AbstractConfigValue =
      if (context.isRestrictedToChild) {
        if (key == context.restrictToChild.first) {
          val remainder = context.restrictToChild.remainder
          if (remainder != null) {
            val result = context.restrict(remainder).resolve(v, source)
            context = result.context.unrestricted.restrict(originalRestrict)
            result.value
          } else {
            // we don't want to resolve the leaf child.
            v
          }
        } else {
          // not in the restrictToChild path
          v
        }
      } else {
        // no restrictToChild, resolve everything
        val result = context.unrestricted.resolve(v, source)
        context = result.context.unrestricted.restrict(originalRestrict)
        result.value
      }
  }

  // this is only Serializable to chill out a findbugs warning
  @SerialVersionUID(1L)
  private object OrderedRenderComparator {
    private def isAllDigits(s: String): Boolean = {
      val length = s.length
      // empty string doesn't count as a number
      // string longer than "max number of digits in a long" cannot be parsed as a long
      if (length == 0) return false
      var i = 0
      var allDigits = true
      while (i < length) {
        breakable {
          val c = s.charAt(i)
          if (!Character.isDigit(c)) {
            allDigits = false
            break() // continue
          }
        }
        if (allDigits) i += 1
        else i = length // exit loop
      }
      allDigits
    }
  }

  @SerialVersionUID(1L)
  private sealed abstract class RenderComparator
      extends ju.Comparator[String]
      with Serializable {
    def compare(a: String, b: String): Int
  }

  @SerialVersionUID(1L)
  final private class OrderedRenderComparator extends RenderComparator {
    // This is supposed to sort numbers before strings,
    // and sort the numbers numerically. The point is
    // to make objects which are really list-like
    // (numeric indices) appear in order.
    override def compare(a: String, b: String): Int = {
      val aDigits = OrderedRenderComparator.isAllDigits(a)
      val bDigits = OrderedRenderComparator.isAllDigits(b)
      if (aDigits && bDigits) new BigInteger(a).compareTo(new BigInteger(b))
      else if (aDigits) -1
      else if (bDigits) 1
      else a.compareTo(b)
    }
  }

  @SerialVersionUID(1L)
  final private class KeepOriginRenderComparator(
      getOriginFor: String => SimpleConfigOrigin
  ) extends RenderComparator {
    override def compare(a: String, b: String): Int = {
      val aOrigin = getOriginFor(a)
      val bOrigin = getOriginFor(b)

      val aFilename = Option(aOrigin.filename).getOrElse("")
      val bFilename = Option(bOrigin.filename).getOrElse("")

      val compareFiles = aFilename compareTo bFilename

      if (compareFiles != 0) compareFiles
      else
        aOrigin.lineNumber compareTo bOrigin.lineNumber
    }
  }

  private def mapEquals(
      a: ju.Map[String, ConfigValue],
      b: ju.Map[String, ConfigValue]
  ) =
    if (a eq b) true
    else if (a.keySet != b.keySet) false
    else !a.keySet.asScala.exists(key => a.get(key) != b.get(key))

  private def mapHash(m: ju.Map[String, ConfigValue]) = {
    // the keys have to be sorted, otherwise we could be equal
    // to another map but have a different hashcode.
    val keys = new ju.ArrayList[String]
    keys.addAll(m.keySet)
    ju.Collections.sort(keys)
    var valuesHash = 0
    for (k <- keys.asScala) {
      valuesHash += m.get(k).hashCode
    }
    41 * (41 + keys.hashCode) + valuesHash
  }

  private val EMPTY_NAME = "empty config"

  private val emptyInstance = empty(SimpleConfigOrigin.newSimple(EMPTY_NAME))

  private[impl] def empty: SimpleConfigObject = emptyInstance

  private[impl] def empty(origin: ConfigOrigin): SimpleConfigObject =
    if (origin == null) empty
    else
      new SimpleConfigObject(
        origin,
        ju.Collections.emptyMap[String, AbstractConfigValue]
      )

  private[impl] def emptyMissing(baseOrigin: ConfigOrigin) =
    new SimpleConfigObject(
      SimpleConfigOrigin.newSimple(baseOrigin.description + " (not found)"),
      ju.Collections.emptyMap[String, AbstractConfigValue]
    )
}

@SerialVersionUID(2L)
final class SimpleConfigObject(
    _origin: ConfigOrigin,
    // this map should never be modified - assume immutable
    val value: ju.Map[String, AbstractConfigValue],
    val status: ResolveStatus,
    override val ignoresFallbacks: Boolean
) extends AbstractConfigObject(_origin)
    with Serializable {
  if (value == null)
    throw new ConfigException.BugOrBroken(
      "creating config object with null map"
    )
  final private var resolved = status eq ResolveStatus.RESOLVED
  // Kind of an expensive debug check. Comment out?
  if (status ne ResolveStatus.fromValues(value.values))
    throw new ConfigException.BugOrBroken("Wrong resolved status on " + this)

  def this(_origin: ConfigOrigin, value: ju.Map[String, AbstractConfigValue]) =
    this(
      _origin,
      value,
      ResolveStatus.fromValues(value.values),
      false /* ignoresFallbacks */
    )

  override def withOnlyKey(key: String): SimpleConfigObject =
    withOnlyPath(Path.newKey(key))

  override def withoutKey(key: String): SimpleConfigObject =
    withoutPath(Path.newKey(key))

  // gets the object with only the path if the path
  // exists, otherwise null if it doesn't. this ensures
  // that if we have { a : { b : 42 } } and do
  // withOnlyPath("a.b.c") that we don't keep an empty
  // "a" object.
  override def withOnlyPathOrNull(path: Path): SimpleConfigObject = {
    val key = path.first
    val next = path.remainder
    var v: AbstractConfigValue = value.get(key)
    if (next != null) {
      if (v != null && v.isInstanceOf[AbstractConfigObject]) {
        v = v.asInstanceOf[AbstractConfigObject].withOnlyPathOrNull(next)
      } else {
        // if the path has more elements but we don't have an object,
        // then the rest of the path does not exist.
        v = null
      }
    }
    if (v == null) null
    else
      new SimpleConfigObject(
        origin,
        ju.Collections.singletonMap(key, v),
        v.resolveStatus,
        ignoresFallbacks
      )
  }

  override def withOnlyPath(path: Path): SimpleConfigObject = {
    val o = withOnlyPathOrNull(path)
    if (o == null)
      new SimpleConfigObject(
        origin,
        ju.Collections.emptyMap[String, AbstractConfigValue],
        ResolveStatus.RESOLVED,
        ignoresFallbacks
      )
    else o
  }

  override def withoutPath(path: Path): SimpleConfigObject = {
    val key = path.first
    val next = path.remainder
    var v = value.get(key)
    if (v != null && next != null && v.isInstanceOf[AbstractConfigObject]) {
      v = v.asInstanceOf[AbstractConfigObject].withoutPath(next)
      val updated = new ju.HashMap[String, AbstractConfigValue](value)
      updated.put(key, v)
      new SimpleConfigObject(
        origin,
        updated,
        ResolveStatus.fromValues(updated.values),
        ignoresFallbacks
      )
    } else if (next != null || v == null) { // can't descend, nothing to remove
      this
    } else {
      val smaller = new ju.HashMap[String, AbstractConfigValue](value.size - 1)
      for (old <- value.entrySet.asScala) {
        if (!(old.getKey == key)) smaller.put(old.getKey, old.getValue)
      }
      new SimpleConfigObject(
        origin,
        smaller,
        ResolveStatus.fromValues(smaller.values),
        ignoresFallbacks
      )
    }
  }

  override def withValue(key: String, v: ConfigValue): SimpleConfigObject = {
    if (v == null)
      throw new ConfigException.BugOrBroken(
        "Trying to store null ConfigValue in a ConfigObject"
      )
    var newMap: ju.Map[String, AbstractConfigValue] = null
    if (value.isEmpty)
      newMap =
        ju.Collections.singletonMap(key, v.asInstanceOf[AbstractConfigValue])
    else {
      newMap = new ju.HashMap[String, AbstractConfigValue](value)
      newMap.put(key, v.asInstanceOf[AbstractConfigValue])
    }
    new SimpleConfigObject(
      origin,
      newMap,
      ResolveStatus.fromValues(newMap.values),
      ignoresFallbacks
    )
  }

  override def withValue(path: Path, v: ConfigValue): SimpleConfigObject = {
    val key = path.first
    val next = path.remainder
    if (next == null) withValue(key, v)
    else {
      val child = value.get(key)
      if (child != null && child
            .isInstanceOf[AbstractConfigObject]) { // if we have an object, add to it
        withValue(
          key,
          child.asInstanceOf[AbstractConfigObject].withValue(next, v)
        )
      } else { // as soon as we have a non-object, replace it entirely
        val subtree = v
          .asInstanceOf[AbstractConfigValue]
          .atPath(
            SimpleConfigOrigin.newSimple("withValue(" + next.render + ")"),
            next
          )
        withValue(key, subtree.root)
      }
    }
  }

  override def attemptPeekWithPartialResolve(key: String): AbstractConfigValue =
    value.get(key)

  private def newCopy(
      newStatus: ResolveStatus,
      newOrigin: ConfigOrigin,
      newIgnoresFallbacks: Boolean
  ) =
    new SimpleConfigObject(newOrigin, value, newStatus, newIgnoresFallbacks)

  override def newCopy(
      newStatus: ResolveStatus,
      newOrigin: ConfigOrigin
  ): SimpleConfigObject =
    newCopy(newStatus, newOrigin, ignoresFallbacks)

  override def withFallbacksIgnored(): SimpleConfigObject =
    if (ignoresFallbacks) this
    else newCopy(resolveStatus, origin, true)

  override def resolveStatus: ResolveStatus =
    ResolveStatus.fromBoolean(resolved)

  override def replaceChild(
      child: AbstractConfigValue,
      replacement: AbstractConfigValue
  ): SimpleConfigObject = {
    val newChildren = new ju.HashMap[String, AbstractConfigValue](value)
    newChildren.entrySet.asScala.find(_.getValue() eq child) match {
      case Some(old) =>
        if (replacement != null) old.setValue(replacement)
        else newChildren.remove(old.getKey)
        new SimpleConfigObject(
          origin,
          newChildren,
          ResolveStatus.fromValues(newChildren.values),
          ignoresFallbacks
        )
      case None =>
        throw new ConfigException.BugOrBroken(
          "SimpleConfigObject.replaceChild did not find " + child + " in " + this
        )
    }
  }

  // related to AbstractConfigValue.hasDescendantInList
  override def hasDescendant(descendant: AbstractConfigValue): Boolean =
    value.values.asScala.exists(_ eq descendant) ||
      // now the expensive traversal
      value.values.asScala.exists(
        _ match {
          case v: Container => v.hasDescendant(descendant)
          case _            => false
        }
      )

  override def unwrapped: ju.Map[String, AnyRef] = {
    val m = new ju.HashMap[String, AnyRef]

    for (e <- value.entrySet.asScala) {
      m.put(e.getKey, e.getValue.unwrapped)
    }
    m
  }

  override def mergedWithObject(
      abstractFallback: AbstractConfigObject
  ): SimpleConfigObject = {
    requireNotIgnoringFallbacks()
    if (!abstractFallback.isInstanceOf[SimpleConfigObject])
      throw new ConfigException.BugOrBroken(
        "should not be reached (merging non-SimpleConfigObject)"
      )
    val fallback = abstractFallback.asInstanceOf[SimpleConfigObject]
    var changed = false
    var allResolved = true
    val merged = new ju.HashMap[String, AbstractConfigValue]
    val allKeys = new ju.HashSet[String]
    allKeys.addAll(this.keySet)
    allKeys.addAll(fallback.keySet)

    for (key <- allKeys.asScala) {
      val first = this.value.get(key)
      val second = fallback.value.get(key)
      var kept: AbstractConfigValue = null
      if (first == null) kept = second
      else if (second == null) kept = first
      else kept = first.withFallback(second)
      merged.put(key, kept)
      if (first ne kept) changed = true
      if (kept.resolveStatus eq ResolveStatus.UNRESOLVED) allResolved = false
    }
    val newResolveStatus = ResolveStatus.fromBoolean(allResolved)
    val newIgnoresFallbacks = fallback.ignoresFallbacks
    if (changed)
      new SimpleConfigObject(
        AbstractConfigObject.mergeOrigins(this, fallback),
        merged,
        newResolveStatus,
        newIgnoresFallbacks
      )
    else if ((newResolveStatus ne resolveStatus) || newIgnoresFallbacks != ignoresFallbacks)
      newCopy(newResolveStatus, origin, newIgnoresFallbacks)
    else this
  }

  private def modify(modifier: AbstractConfigValue.NoExceptionsModifier) =
    try modifyMayThrow(modifier)
    catch {
      case e: RuntimeException =>
        throw e
      case e: Exception =>
        throw new ConfigException.BugOrBroken("unexpected checked exception", e)
    }

  @throws[Exception]
  private def modifyMayThrow(modifier: AbstractConfigValue.Modifier) = {
    var changes: ju.Map[String, AbstractConfigValue] = null
    for (k <- keySet.asScala) {
      val v = value.get(k)
      // "modified" may be null, which means remove the child;
      // to do that we put null in the "changes" map.
      val modified = modifier.modifyChildMayThrow(k, v)
      if (modified ne v) {
        if (changes == null)
          changes = new ju.HashMap[String, AbstractConfigValue]
        changes.put(k, modified)
      }
    }
    if (changes == null) this
    else {
      val modified = new ju.HashMap[String, AbstractConfigValue]
      var sawUnresolved = false
      for (k <- keySet.asScala) {
        if (changes.containsKey(k)) {
          val newValue = changes.get(k)
          if (newValue != null) {
            modified.put(k, newValue)
            if (newValue.resolveStatus eq ResolveStatus.UNRESOLVED)
              sawUnresolved = true
          } else {
            // remove this child; don't put it in the new map.
          }
        } else {
          val newValue = value.get(k)
          modified.put(k, newValue)
          if (newValue.resolveStatus eq ResolveStatus.UNRESOLVED)
            sawUnresolved = true
        }
      }
      new SimpleConfigObject(
        origin,
        modified,
        if (sawUnresolved) ResolveStatus.UNRESOLVED
        else ResolveStatus.RESOLVED,
        ignoresFallbacks
      )
    }
  }

  @throws[NotPossibleToResolve]
  override def resolveSubstitutions(
      context: ResolveContext,
      source: ResolveSource
  ): ResolveResult[_ <: AbstractConfigObject] = {
    if (resolveStatus eq ResolveStatus.RESOLVED)
      return ResolveResult.make(context, this)
    val sourceWithParent = source.pushParent(this)
    try {
      val modifier =
        new SimpleConfigObject.ResolveModifier(context, sourceWithParent)
      val value = modifyMayThrow(modifier)
      ResolveResult.make(modifier.context, value).asObjectResult
    } catch {
      case e: AbstractConfigValue.NotPossibleToResolve =>
        throw e
      case e: RuntimeException =>
        throw e
      case e: Exception =>
        throw new ConfigException.BugOrBroken("unexpected checked exception", e)
    }
  }

  override def relativized(prefix: Path): SimpleConfigObject =
    modify(new AbstractConfigValue.NoExceptionsModifier() {
      override def modifyChild(
          key: String,
          v: AbstractConfigValue
      ): AbstractConfigValue =
        v.relativized(prefix)
    })

  private def trySimplifyTheOnlyNestedObjectRec(
      keysAggregate: String
  ): Option[(String, AbstractConfigValue)] =
    if (value.size() == 1) {
      val newKeyElement = ConfigImplUtil.renderStringUnquotedIfPossible(
        keySet.iterator().next()
      )
      val newAggregate = (if (keysAggregate.isEmpty) ""
                          else s"$keysAggregate.") + newKeyElement
      values.iterator().next() match {
        case innerSCO: SimpleConfigObject =>
          innerSCO.trySimplifyTheOnlyNestedObjectRec(newAggregate)
        case other: AbstractConfigValue =>
          Some(newAggregate -> other)
        case _ => None
      }
    } else None

  private def trySimplifyTheOnlyNestedObject(
      options: ConfigRenderOptions
  ): Option[(String, AbstractConfigValue)] =
    if (!options.getConfigFormatOptions.getSimplifyNestedObjects || options.getJson) {
      None
    } else trySimplifyTheOnlyNestedObjectRec("")

  override def renderValue(
      sb: jl.StringBuilder,
      indentVal: Int,
      atRoot: Boolean,
      options: ConfigRenderOptions
  ): Unit = {
    if (isEmpty) sb.append("{}")
    else {
      trySimplifyTheOnlyNestedObject(options) match {
        case Some((aggKey, leafValue)) =>
          if (!atRoot) { // if not root then it is in some other object
            if (options.getFormatted)
              sb.deleteCharAt(
                sb.length() - 1
              ) // assumption that ' ' is the removed char
            sb.append('.')
          }
          leafValue.renderWithRenderedKey(sb, aggKey, options)
          leafValue.renderValue(sb, indentVal, atRoot, options)
        case _ =>
          renderValueAsMultiLineObject(sb, indentVal, atRoot, options)
      }
    }
    if (atRoot && options.getFormatted && options.getConfigFormatOptions.getNewLineAtEnd)
      sb.append('\n')
  }

  private def renderValueAsMultiLineObject(
      sb: jl.StringBuilder,
      indentVal: Int,
      atRoot: Boolean,
      options: ConfigRenderOptions
  ): Unit = {
    val outerBraces = options.getJson || !atRoot
    var innerIndent = 0
    if (outerBraces) {
      innerIndent = indentVal + 1
      sb.append("{")
      if (options.getFormatted) sb.append('\n')
    } else innerIndent = indentVal
    var separatorCount = 0

    val keys = new ju.ArrayList[String]
    keys.addAll(keySet)
    val ordering =
      if (options.getConfigFormatOptions.getKeepOriginOrder)
        new SimpleConfigObject.KeepOriginRenderComparator(str =>
          value.get(str).origin
        )
      else new SimpleConfigObject.OrderedRenderComparator
    ju.Collections.sort(keys, ordering)

    for (k <- keys.asScala) {
      var v: AbstractConfigValue = null
      v = value.get(k)
      if (options.getOriginComments) {
        val lines = v.origin.description.split("\n")
        for (l <- lines) {
          AbstractConfigValue.indent(sb, indentVal + 1, options)
          sb.append('#')
          if (!l.isEmpty) sb.append(' ')
          sb.append(l)
          sb.append("\n")
        }
      }
      if (options.getComments) {
        for (comment <- v.origin.comments.asScala) {
          AbstractConfigValue.indent(sb, innerIndent, options)
          sb.append("#")
          if (!comment.startsWith(" ")) sb.append(' ')
          sb.append(comment)
          sb.append("\n")
        }
      }
      AbstractConfigValue.indent(sb, innerIndent, options)
      v.render(sb, innerIndent, false /* atRoot */, k, options)
      if (options.getFormatted) {
        if (options.getJson) {
          sb.append(",")
          separatorCount = 2
        } else separatorCount = 1
        sb.append('\n')
      } else {
        sb.append(",")
        separatorCount = 1
      }
    }
    // chop last commas/newlines
    sb.setLength(sb.length - separatorCount)
    if (outerBraces) {
      if (options.getFormatted) {
        sb.append('\n') // put a newline back

        if (outerBraces)
          AbstractConfigValue.indent(sb, indentVal, options)
      }
      sb.append("}")
    }
  }

  override def get(key: Any): AbstractConfigValue = value.get(key)

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ConfigObject]

  override def equals(other: Any): Boolean = {
    // note that "origin" is deliberately NOT part of equality.
    // neither are other "extras" like ignoresFallbacks or resolve status.
    if (other.isInstanceOf[ConfigObject]) {
      // optimization to avoid unwrapped() for two ConfigObject,
      // which is what AbstractConfigValue does.
      canEqual(other) && SimpleConfigObject.mapEquals(
        this,
        other.asInstanceOf[ConfigObject]
      )
    } else false
  }

  override def hashCode
      : Int = { // note that "origin" is deliberately NOT part of equality
    SimpleConfigObject.mapHash(this)
  }

  override def containsKey(key: Any): Boolean = value.containsKey(key)

  override def keySet: ju.Set[String] = value.keySet

  override def containsValue(v: Any): Boolean = value.containsValue(v)

  override def entrySet: ju.Set[ju.Map.Entry[String, ConfigValue]] = {
    // total bloat just to work around lack of type variance
    val entries = new ju.HashSet[ju.Map.Entry[String, ConfigValue]]
    for (e <- value.entrySet.asScala) {
      entries.add(
        new ju.AbstractMap.SimpleImmutableEntry[String, ConfigValue](
          e.getKey,
          e.getValue
        )
      )
    }
    entries
  }

  override def isEmpty: Boolean = value.isEmpty

  override def size: Int = value.size

  override def values: ju.Collection[ConfigValue] =
    new ju.HashSet[ConfigValue](value.values)

  // serialization all goes through SerializedConfigValue
  @throws[ObjectStreamException]
  private def writeReplace(): Object = new SerializedConfigValue(this)
}
