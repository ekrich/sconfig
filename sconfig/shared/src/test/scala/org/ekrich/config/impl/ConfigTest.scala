/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import scala.jdk.CollectionConverters._

import org.junit.Assert._
import org.junit._

import org.ekrich.config._
import org.ekrich.config.ConfigResolveOptions

class ConfigTest extends TestUtilsShared {
  private def resolveNoSystem(
      v: AbstractConfigValue,
      root: AbstractConfigObject
  ) = {
    ResolveContext.resolve(v, root, ConfigResolveOptions.noSystem)
  }

  private def resolveNoSystem(v: SimpleConfig, root: SimpleConfig) = {
    ResolveContext
      .resolve(v.root, root.root, ConfigResolveOptions.noSystem)
      .asInstanceOf[AbstractConfigObject]
      .toConfig
  }

  def mergeUnresolved(toMerge: AbstractConfigObject*) = {
    if (toMerge.isEmpty) {
      SimpleConfigObject.empty()
    } else {
      toMerge.reduce((first, second) => first.withFallback(second))
    }
  }

  def merge(toMerge: AbstractConfigObject*) = {
    val obj = mergeUnresolved(toMerge: _*)
    resolveNoSystem(obj, obj) match {
      case x: AbstractConfigObject => x
    }
  }

  // Merging should always be associative (same results however the values are grouped,
  // as long as they remain in the same order)
  private def associativeMerge(
      allObjects: Seq[AbstractConfigObject]
  )(assertions: SimpleConfig => Unit): Unit = {
    def makeTrees(
        objects: Seq[AbstractConfigObject]
    ): Iterator[AbstractConfigObject] = {
      objects.length match {
        case 0 => Iterator.empty
        case 1 => {
          Iterator(objects(0))
        }
        case 2 => {
          Iterator(objects(0).withFallback(objects(1)))
        }
        case n => {
          val leftSplits = for {
            i <- (1 until n)
            pair = objects.splitAt(i)
            first = pair._1.reduceLeft(_.withFallback(_))
            second = pair._2.reduceLeft(_.withFallback(_))
          } yield first.withFallback(second)
          val rightSplits = for {
            i <- (1 until n)
            pair = objects.splitAt(i)
            first = pair._1.reduceRight(_.withFallback(_))
            second = pair._2.reduceRight(_.withFallback(_))
          } yield first.withFallback(second)
          leftSplits.iterator ++ rightSplits.iterator
        }
      }
    }

    val trees = makeTrees(allObjects).toSeq
    for (tree <- trees) {
      // if this fails, we were not associative.
      if (!trees(0).equals(tree))
        throw new AssertionError(
          "Merge was not associative, " +
            "verify that it should not be, then don't use associativeMerge " +
            "for this one. two results were: \none: " + trees(0) + "\ntwo: " +
            tree + "\noriginal list: " + allObjects
        )
    }

    for (tree <- trees) {
      assertions(tree.toConfig)
    }
  }

  @Test
  def mergeTrivial(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "b" : 2 }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(2, merged.getInt("b"))
    assertEquals(2, merged.root.size)
  }

  @Test
  def mergeEmpty(): Unit = {
    val merged = merge().toConfig

    assertEquals(0, merged.root.size)
  }

  @Test
  def mergeOne(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val merged = merge(obj1).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(1, merged.root.size)
  }

  @Test
  def mergeOverride(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "a" : 2 }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(1, merged.root.size)

    val merged2 = merge(obj2, obj1).toConfig

    assertEquals(2, merged2.getInt("a"))
    assertEquals(1, merged2.root.size)
  }

  @Test
  def mergeN(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "b" : 2 }""")
    val obj3 = parseObject("""{ "c" : 3 }""")
    val obj4 = parseObject("""{ "d" : 4 }""")

    associativeMerge(Seq(obj1, obj2, obj3, obj4)) { merged =>
      assertEquals(1, merged.getInt("a"))
      assertEquals(2, merged.getInt("b"))
      assertEquals(3, merged.getInt("c"))
      assertEquals(4, merged.getInt("d"))
      assertEquals(4, merged.root.size)
    }
  }

  @Test
  def mergeOverrideN(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "a" : 2 }""")
    val obj3 = parseObject("""{ "a" : 3 }""")
    val obj4 = parseObject("""{ "a" : 4 }""")
    associativeMerge(Seq(obj1, obj2, obj3, obj4)) { merged =>
      assertEquals(1, merged.getInt("a"))
      assertEquals(1, merged.root.size)
    }

    associativeMerge(Seq(obj4, obj3, obj2, obj1)) { merged2 =>
      assertEquals(4, merged2.getInt("a"))
      assertEquals(1, merged2.root.size)
    }
  }

  @Test
  def mergeNested(): Unit = {
    val obj1 = parseObject("""{ "root" : { "a" : 1, "z" : 101 } }""")
    val obj2 = parseObject("""{ "root" : { "b" : 2, "z" : 102 } }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("root.a"))
    assertEquals(2, merged.getInt("root.b"))
    assertEquals(101, merged.getInt("root.z"))
    assertEquals(1, merged.root.size)
    assertEquals(3, merged.getConfig("root").root.size)
  }

  @Test
  def mergeWithEmpty(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(1, merged.root.size)

    val merged2 = merge(obj2, obj1).toConfig

    assertEquals(1, merged2.getInt("a"))
    assertEquals(1, merged2.root.size)
  }

  @Test
  def mergeOverrideObjectAndPrimitive(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "a" : { "b" : 42 } }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(1, merged.root.size)

    val merged2 = merge(obj2, obj1).toConfig

    assertEquals(42, merged2.getConfig("a").getInt("b"))
    assertEquals(42, merged2.getInt("a.b"))
    assertEquals(1, merged2.root.size)
    assertEquals(1, merged2.getObject("a").size)
  }

  @Test
  def mergeOverrideObjectAndSubstitution(): Unit = {
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "a" : { "b" : ${c} }, "c" : 42 }""")
    val merged = merge(obj1, obj2).toConfig

    assertEquals(1, merged.getInt("a"))
    assertEquals(2, merged.root.size)

    val merged2 = merge(obj2, obj1).toConfig

    assertEquals(42, merged2.getConfig("a").getInt("b"))
    assertEquals(42, merged2.getInt("a.b"))
    assertEquals(2, merged2.root.size)
    assertEquals(1, merged2.getObject("a").size)
  }

  @Test
  def mergeObjectThenPrimitiveThenObject(): Unit = {
    // the semantic here is that the primitive blocks the
    // object that occurs at lower priority. This is consistent
    // with duplicate keys in the same file.
    val obj1 = parseObject("""{ "a" : { "b" : 42 } }""")
    val obj2 = parseObject("""{ "a" : 2 }""")
    val obj3 = parseObject("""{ "a" : { "b" : 43, "c" : 44 } }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      assertEquals(42, merged.getInt("a.b"))
      assertEquals(1, merged.root.size)
      assertEquals(1, merged.getObject("a").size())
    }

    associativeMerge(Seq(obj3, obj2, obj1)) { merged2 =>
      assertEquals(43, merged2.getInt("a.b"))
      assertEquals(44, merged2.getInt("a.c"))
      assertEquals(1, merged2.root.size)
      assertEquals(2, merged2.getObject("a").size())
    }
  }

  @Test
  def mergeObjectThenSubstitutionThenObject(): Unit = {
    // the semantic here is that the primitive blocks the
    // object that occurs at lower priority. This is consistent
    // with duplicate keys in the same file.
    val obj1 = parseObject("""{ "a" : { "b" : ${f} } }""")
    val obj2 = parseObject("""{ "a" : 2 }""")
    val obj3 = parseObject(
      """{ "a" : { "b" : ${d}, "c" : ${e} }, "d" : 43, "e" : 44, "f" : 42 }"""
    )

    associativeMerge(Seq(obj1, obj2, obj3)) { unresolved =>
      val merged = resolveNoSystem(unresolved, unresolved)
      assertEquals(42, merged.getInt("a.b"))
      assertEquals(4, merged.root.size)
      assertEquals(1, merged.getObject("a").size())
    }

    associativeMerge(Seq(obj3, obj2, obj1)) { unresolved =>
      val merged2 = resolveNoSystem(unresolved, unresolved)
      assertEquals(43, merged2.getInt("a.b"))
      assertEquals(44, merged2.getInt("a.c"))
      assertEquals(4, merged2.root.size)
      assertEquals(2, merged2.getObject("a").size())
    }
  }

  @Test
  def mergePrimitiveThenObjectThenPrimitive(): Unit = {
    // the primitive should override the object
    val obj1 = parseObject("""{ "a" : 1 }""")
    val obj2 = parseObject("""{ "a" : { "b" : 42 } }""")
    val obj3 = parseObject("""{ "a" : 3 }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      assertEquals(1, merged.getInt("a"))
      assertEquals(1, merged.root.size)
    }
  }

  @Test
  def mergeSubstitutionThenObjectThenSubstitution(): Unit = {
    // the substitution should override the object
    val obj1 = parseObject("""{ "a" : ${b}, "b" : 1 }""")
    val obj2 = parseObject("""{ "a" : { "b" : 42 } }""")
    val obj3 = parseObject("""{ "a" : ${c}, "c" : 2 }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      val resolved = resolveNoSystem(merged, merged)

      assertEquals(1, resolved.getInt("a"))
      assertEquals(3, resolved.root.size)
    }
  }

  @Test
  def mergeSubstitutedValues(): Unit = {
    val obj1 = parseObject("""{ "a" : { "x" : 1, "z" : 4 }, "c" : ${a} }""")
    val obj2 = parseObject("""{ "b" : { "y" : 2, "z" : 5 }, "c" : ${b} }""")

    val resolved = merge(obj1, obj2).toConfig

    assertEquals(3, resolved.getObject("c").size())
    assertEquals(1, resolved.getInt("c.x"))
    assertEquals(2, resolved.getInt("c.y"))
    assertEquals(4, resolved.getInt("c.z"))
  }

  @Test
  def mergeObjectWithSubstituted(): Unit = {
    val obj1 = parseObject(
      """{ "a" : { "x" : 1, "z" : 4 }, "c" : { "z" : 42 } }"""
    )
    val obj2 = parseObject("""{ "b" : { "y" : 2, "z" : 5 }, "c" : ${b} }""")

    val resolved = merge(obj1, obj2).toConfig

    assertEquals(2, resolved.getObject("c").size())
    assertEquals(2, resolved.getInt("c.y"))
    assertEquals(42, resolved.getInt("c.z"))

    val resolved2 = merge(obj2, obj1).toConfig

    assertEquals(2, resolved2.getObject("c").size())
    assertEquals(2, resolved2.getInt("c.y"))
    assertEquals(5, resolved2.getInt("c.z"))
  }

  private val cycleObject = {
    parseObject("""
{
    "foo" : ${bar},
    "bar" : ${a.b.c},
    "a" : { "b" : { "c" : ${foo} } }
}
""")
  }

  @Test
  def mergeHidesCycles(): Unit = {
    // the point here is that we should not try to evaluate a substitution
    // that's been overridden, and thus not end up with a cycle as long
    // as we override the problematic link in the cycle.
    val e = intercept[ConfigException.UnresolvedSubstitution] {
      val v = resolveNoSystem(subst("foo"), cycleObject)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage().contains("cycle")
    )

    val fixUpCycle = parseObject(""" { "a" : { "b" : { "c" : 57 } } } """)
    val merged = mergeUnresolved(fixUpCycle, cycleObject)
    val v = resolveNoSystem(subst("foo"), merged)
    assertEquals(intValue(57), v)
  }

  @Test
  def mergeWithObjectInFrontKeepsCycles(): Unit = {
    // the point here is that if our eventual value will be an object, then
    // we have to evaluate the substitution to see if it's an object to merge,
    // so we don't avoid the cycle.
    val e = intercept[ConfigException.UnresolvedSubstitution] {
      val v = resolveNoSystem(subst("foo"), cycleObject)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage().contains("cycle")
    )

    val fixUpCycle = parseObject(
      """ { "a" : { "b" : { "c" : { "q" : "u" } } } } """
    )
    val merged = mergeUnresolved(fixUpCycle, cycleObject)
    val e2 = intercept[ConfigException.UnresolvedSubstitution] {
      val v = resolveNoSystem(subst("foo"), merged)
    }
    // TODO: it would be nicer if the above threw BadValue with an
    // explanation about the cycle.
    // assertTrue(e2.getMessage().contains("cycle"))
  }

  @Test
  def mergeSeriesOfSubstitutions(): Unit = {
    val obj1 = parseObject("""{ "a" : { "x" : 1, "q" : 4 }, "j" : ${a} }""")
    val obj2 = parseObject("""{ "b" : { "y" : 2, "q" : 5 }, "j" : ${b} }""")
    val obj3 = parseObject("""{ "c" : { "z" : 3, "q" : 6 }, "j" : ${c} }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      val resolved = resolveNoSystem(merged, merged)

      assertEquals(4, resolved.getObject("j").size())
      assertEquals(1, resolved.getInt("j.x"))
      assertEquals(2, resolved.getInt("j.y"))
      assertEquals(3, resolved.getInt("j.z"))
      assertEquals(4, resolved.getInt("j.q"))
    }
  }

  @Test
  def mergePrimitiveAndTwoSubstitutions(): Unit = {
    val obj1 = parseObject("""{ "j" : 42 }""")
    val obj2 = parseObject("""{ "b" : { "y" : 2, "q" : 5 }, "j" : ${b} }""")
    val obj3 = parseObject("""{ "c" : { "z" : 3, "q" : 6 }, "j" : ${c} }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      val resolved = resolveNoSystem(merged, merged)

      assertEquals(3, resolved.root.size())
      assertEquals(42, resolved.getInt("j"))
      assertEquals(2, resolved.getInt("b.y"))
      assertEquals(3, resolved.getInt("c.z"))
    }
  }

  @Test
  def mergeObjectAndTwoSubstitutions(): Unit = {
    val obj1 = parseObject("""{ "j" : { "x" : 1, "q" : 4 } }""")
    val obj2 = parseObject("""{ "b" : { "y" : 2, "q" : 5 }, "j" : ${b} }""")
    val obj3 = parseObject("""{ "c" : { "z" : 3, "q" : 6 }, "j" : ${c} }""")

    associativeMerge(Seq(obj1, obj2, obj3)) { merged =>
      val resolved = resolveNoSystem(merged, merged)

      assertEquals(4, resolved.getObject("j").size())
      assertEquals(1, resolved.getInt("j.x"))
      assertEquals(2, resolved.getInt("j.y"))
      assertEquals(3, resolved.getInt("j.z"))
      assertEquals(4, resolved.getInt("j.q"))
    }
  }

  @Test
  def mergeObjectSubstitutionObjectSubstitution(): Unit = {
    val obj1 = parseObject("""{ "j" : { "w" : 1, "q" : 5 } }""")
    val obj2 = parseObject("""{ "b" : { "x" : 2, "q" : 6 }, "j" : ${b} }""")
    val obj3 = parseObject("""{ "j" : { "y" : 3, "q" : 7 } }""")
    val obj4 = parseObject("""{ "c" : { "z" : 4, "q" : 8 }, "j" : ${c} }""")

    associativeMerge(Seq(obj1, obj2, obj3, obj4)) { merged =>
      val resolved = resolveNoSystem(merged, merged)

      assertEquals(5, resolved.getObject("j").size())
      assertEquals(1, resolved.getInt("j.w"))
      assertEquals(2, resolved.getInt("j.x"))
      assertEquals(3, resolved.getInt("j.y"))
      assertEquals(4, resolved.getInt("j.z"))
      assertEquals(5, resolved.getInt("j.q"))
    }
  }

  private def ignoresFallbacks(m: ConfigMergeable) = {
    m match {
      case v: AbstractConfigValue =>
        v.ignoresFallbacks
      case c: SimpleConfig =>
        c.root.ignoresFallbacks
    }
  }

  private def testIgnoredMergesDoNothing(nonEmpty: ConfigMergeable): Unit = {
    // falling back to a primitive once should switch us to "ignoreFallbacks" mode
    // and then twice should "return this". Falling back to an empty object should
    // return this unless the empty object was ignoreFallbacks and then we should
    // "catch" its ignoreFallbacks.

    // some of what this tests is just optimization, not API contract (withFallback
    // can return a new object anytime it likes) but want to be sure we do the
    // optimizations.

    val empty = SimpleConfigObject.empty(null)
    val primitive = intValue(42)
    val emptyIgnoringFallbacks = empty.withFallback(primitive)
    val nonEmptyIgnoringFallbacks = nonEmpty.withFallback(primitive)

    assertEquals(false, empty.ignoresFallbacks)
    assertEquals(true, primitive.ignoresFallbacks)
    assertEquals(true, emptyIgnoringFallbacks.ignoresFallbacks)
    assertEquals(false, ignoresFallbacks(nonEmpty))
    assertEquals(true, ignoresFallbacks(nonEmptyIgnoringFallbacks))

    assertTrue(nonEmpty ne nonEmptyIgnoringFallbacks)
    assertTrue(empty ne emptyIgnoringFallbacks)

    // falling back from one object to another should not make us ignore fallbacks
    assertEquals(false, ignoresFallbacks(nonEmpty.withFallback(empty)))
    assertEquals(false, ignoresFallbacks(empty.withFallback(nonEmpty)))
    assertEquals(false, ignoresFallbacks(empty.withFallback(empty)))
    assertEquals(false, ignoresFallbacks(nonEmpty.withFallback(nonEmpty)))

    // falling back from primitive just returns this
    assertTrue(primitive eq primitive.withFallback(empty))
    assertTrue(primitive eq primitive.withFallback(nonEmpty))
    assertTrue(primitive eq primitive.withFallback(nonEmptyIgnoringFallbacks))

    // falling back again from an ignoreFallbacks should be a no-op, return this
    assertTrue(
      nonEmptyIgnoringFallbacks eq nonEmptyIgnoringFallbacks.withFallback(empty)
    )
    assertTrue(
      nonEmptyIgnoringFallbacks eq nonEmptyIgnoringFallbacks
        .withFallback(primitive)
    )
    assertTrue(
      emptyIgnoringFallbacks eq emptyIgnoringFallbacks.withFallback(empty)
    )
    assertTrue(
      emptyIgnoringFallbacks eq emptyIgnoringFallbacks.withFallback(primitive)
    )
  }

  @Test
  def ignoredMergesDoNothing(): Unit = {
    val conf = parseConfig("{ a : 1 }")
    testIgnoredMergesDoNothing(conf)
  }

  @Test
  def testNoMergeAcrossArray(): Unit = {
    val conf = parseConfig("a: {b:1}, a: [2,3], a:{c:4}")
    assertFalse("a.b found in: " + conf, conf.hasPath("a.b"))
    assertTrue("a.c not found in: " + conf, conf.hasPath("a.c"))
  }

  @Test
  def testNoMergeAcrossUnresolvedArray(): Unit = {
    val conf = parseConfig("a: {b:1}, a: [2,${x}], a:{c:4}, x: 42")
    assertFalse("a.b found in: " + conf, conf.hasPath("a.b"))
    assertTrue("a.c not found in: " + conf, conf.hasPath("a.c"))
  }

  @Test
  def testNoMergeLists(): Unit = {
    val conf = parseConfig("a: [1,2], a: [3,4]")
    assertEquals("lists did not merge", Seq(3, 4), conf.getIntList("a").asScala)
  }

  @Test
  def testListsWithFallback(): Unit = {
    val list1 = ConfigValueFactory.fromIterable(Seq(1, 2, 3).asJava)
    val list2 = ConfigValueFactory.fromIterable(Seq(4, 5, 6).asJava)
    val merged1 = list1.withFallback(list2)
    val merged2 = list2.withFallback(list1)
    assertEquals("lists did not merge 1", list1, merged1)
    assertEquals("lists did not merge 2", list2, merged2)
    assertFalse("equals is working on these", list1 == list2)
    assertFalse("equals is working on these", list1 == merged2)
    assertFalse("equals is working on these", list2 == merged1)
  }

  @Test
  def integerRangeChecks(): Unit = {
    val conf = parseConfig(
      "{ tooNegative: " + (Integer.MIN_VALUE - 1L) + ", tooPositive: " + (Integer.MAX_VALUE + 1L) + "}"
    )
    val en = intercept[ConfigException.WrongType] {
      conf.getInt("tooNegative")
    }
    assertTrue(en.getMessage.contains("range"))

    val ep = intercept[ConfigException.WrongType] {
      conf.getInt("tooPositive")
    }
    assertTrue(ep.getMessage.contains("range"))
  }

  @Test
  def isResolvedWorks(): Unit = {
    val resolved = ConfigFactory.parseString("foo = 1")
    assertTrue(
      "config with no substitutions starts as resolved",
      resolved.isResolved
    )
    val unresolved = ConfigFactory.parseString("foo = ${a}, a=42")
    assertFalse(
      "config with substitutions starts as not resolved",
      unresolved.isResolved
    )
    val resolved2 = unresolved.resolve()
    assertTrue("after resolution, config is now resolved", resolved2.isResolved)
  }

  @Test
  def allowUnresolvedDoesAllowUnresolvedArrayElements(): Unit = {
    val values = ConfigFactory.parseString("unknown = [someVal], known = 42")
    val unresolved = ConfigFactory.parseString(
      "concat = [${unknown}[]], sibling = [${unknown}, ${known}]"
    )
    unresolved.resolve(ConfigResolveOptions.defaults.setAllowUnresolved(true))
    unresolved.withFallback(values).resolve()
    unresolved.resolveWith(values)
  }

  @Test
  def allowUnresolvedDoesAllowUnresolved(): Unit = {
    val values = ConfigFactory.parseString("{ foo = 1, bar = 2, m = 3, n = 4}")
    assertTrue(
      "config with no substitutions starts as resolved",
      values.isResolved
    )
    val unresolved = ConfigFactory.parseString(
      "a = ${foo}, b = ${bar}, c { x = ${m}, y = ${n}, z = foo${m}bar }, alwaysResolveable=${alwaysValue}, alwaysValue=42"
    )
    assertFalse(
      "config with substitutions starts as not resolved",
      unresolved.isResolved
    )

    // resolve() by default throws with unresolveable substs
    intercept[ConfigException.UnresolvedSubstitution] {
      unresolved.resolve(ConfigResolveOptions.defaults)
    }
    // we shouldn't be able to get a value without resolving it
    intercept[ConfigException.NotResolved] {
      unresolved.getInt("alwaysResolveable")
    }
    val allowedUnresolved =
      unresolved.resolve(ConfigResolveOptions.defaults.setAllowUnresolved(true))
    // when we partially-resolve we should still resolve what we can
    assertEquals(
      "we resolved the resolveable",
      42,
      allowedUnresolved.getInt("alwaysResolveable")
    )
    // but unresolved should still all throw
    for (k <- Seq("a", "b", "c.x", "c.y")) {
      intercept[ConfigException.NotResolved] { allowedUnresolved.getInt(k) }
    }
    intercept[ConfigException.NotResolved] {
      allowedUnresolved.getString("c.z")
    }

    // and the partially-resolved thing is not resolved
    assertFalse(
      "partially-resolved object is not resolved",
      allowedUnresolved.isResolved
    )

    // scope "val resolved"
    {
      // and given the values for the resolve, we should be able to
      val resolved = allowedUnresolved.withFallback(values).resolve()
      for (kv <- Seq("a" -> 1, "b" -> 2, "c.x" -> 3, "c.y" -> 4)) {
        assertEquals(kv._2, resolved.getInt(kv._1))
      }
      assertEquals("foo3bar", resolved.getString("c.z"))
      assertTrue("fully resolved object is resolved", resolved.isResolved)
    }

    // we should also be able to use resolveWith
    {
      val resolved = allowedUnresolved.resolveWith(values)
      for (kv <- Seq("a" -> 1, "b" -> 2, "c.x" -> 3, "c.y" -> 4)) {
        assertEquals(kv._2, resolved.getInt(kv._1))
      }
      assertEquals("foo3bar", resolved.getString("c.z"))
      assertTrue("fully resolved object is resolved", resolved.isResolved)
    }
  }

  @Test
  def resolveWithWorks(): Unit = {
    // the a=42 is present here to be sure it gets ignored when we resolveWith
    val unresolved = ConfigFactory.parseString("foo = ${a}, a = 42")
    assertEquals(42, unresolved.resolve().getInt("foo"))
    val source = ConfigFactory.parseString("a = 43")
    val resolved = unresolved.resolveWith(source)
    assertEquals(43, resolved.getInt("foo"))
  }

  /**
   * A resolver that replaces paths that start with a particular prefix with
   * strings where that prefix has been replaced with another prefix.
   */
  class DummyResolver(
      prefix: String,
      newPrefix: String,
      fallback: ConfigResolver
  ) extends ConfigResolver {
    override def lookup(path: String): ConfigValue = {
      if (path.startsWith(prefix))
        ConfigValueFactory.fromAnyRef(newPrefix + path.substring(prefix.length))
      else if (fallback != null)
        fallback.lookup(path)
      else
        null
    }

    override def withFallback(f: ConfigResolver): ConfigResolver = {
      if (fallback == null)
        new DummyResolver(prefix, newPrefix, f)
      else
        new DummyResolver(prefix, newPrefix, fallback.withFallback(f))
    }
  }

  private def runFallbackTest(
      expected: String,
      source: String,
      allowUnresolved: Boolean,
      resolvers: ConfigResolver*
  ) = {
    val unresolved = ConfigFactory.parseString(source)
    var options =
      ConfigResolveOptions.defaults.setAllowUnresolved(allowUnresolved)
    for (resolver <- resolvers)
      options = options.appendResolver(resolver)
    val obj = unresolved.resolve(options).root
    assertEquals(
      expected,
      obj.render(ConfigRenderOptions.concise.setJson(false))
    )
  }

  @Test
  def resolveFallback(): Unit = {
    runFallbackTest(
      "x=a,y=b",
      "x=${a},y=${b}",
      false,
      new DummyResolver("", "", null)
    )
    runFallbackTest(
      "x=\"a.b.c\",y=\"a.b.d\"",
      "x=${a.b.c},y=${a.b.d}",
      false,
      new DummyResolver("", "", null)
    )
    runFallbackTest(
      "x=${a.b.c},y=${a.b.d}",
      "x=${a.b.c},y=${a.b.d}",
      true,
      new DummyResolver("x.", "", null)
    )
    runFallbackTest(
      "x=${a.b.c},y=\"e.f\"",
      "x=${a.b.c},y=${d.e.f}",
      true,
      new DummyResolver("d.", "", null)
    )
    runFallbackTest(
      "w=\"Y.c.d\",x=${a},y=\"X.b\",z=\"Y.c\"",
      "x=${a},y=${a.b},z=${a.b.c},w=${a.b.c.d}",
      true,
      new DummyResolver("a.b.", "Y.", null),
      new DummyResolver("a.", "X.", null)
    )

    runFallbackTest(
      "x=${a.b.c}",
      "x=${a.b.c}",
      true,
      new DummyResolver("x.", "", null)
    )
    val e = intercept[ConfigException.UnresolvedSubstitution] {
      runFallbackTest(
        "x=${a.b.c}",
        "x=${a.b.c}",
        false,
        new DummyResolver("x.", "", null)
      )
    }
    assertTrue(e.getMessage.contains("${a.b.c}"))
  }
}
