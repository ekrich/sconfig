/**
 * Copyright (C) 2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigException

import scala.jdk.CollectionConverters._

class ConcatenationTest extends TestUtilsShared {
  @Test
  def noSubstitutionsStringConcat(): Unit = {
    val conf = parseConfig(""" a :  true "xyz" 123 foo  """).resolve()
    assertEquals("true xyz 123 foo", conf.getString("a"))
  }

  @Test
  def trivialStringConcat(): Unit = {
    val conf = parseConfig(""" a : ${x}foo, x = 1 """).resolve()
    assertEquals("1foo", conf.getString("a"))
  }

  @Test
  def twoSubstitutionsStringConcat(): Unit = {
    val conf = parseConfig(""" a : ${x}foo${x}, x = 1 """).resolve()
    assertEquals("1foo1", conf.getString("a"))
  }

  @Test
  def stringConcatCannotSpanLines(): Unit = {
    val e = intercept[ConfigException.Parse] {
      parseConfig(""" a : ${x}
                foo, x = 1 """)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("not be followed") &&
        e.getMessage.contains("','")
    )
  }

  @Test
  def noObjectsInStringConcat(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a : abc { x : y } """)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("abc") &&
        e.getMessage.contains("""{"x":"y"}""")
    )
  }

  @Test
  def noObjectConcatWithNull(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a : null { x : y } """)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("null") &&
        e.getMessage.contains("""{"x":"y"}""")
    )
  }

  @Test
  def noArraysInStringConcat(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a : abc [1, 2] """)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("abc") &&
        e.getMessage.contains("[1,2]")
    )
  }

  @Test
  def noObjectsSubstitutedInStringConcat(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a : abc ${x}, x : { y : z } """).resolve()
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("abc")
    )
  }

  @Test
  def noArraysSubstitutedInStringConcat(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a : abc ${x}, x : [1,2] """).resolve()
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("abc")
    )
  }

  @Test
  def noSubstitutionsListConcat(): Unit = {
    val conf = parseConfig(""" a :  [1,2] [3,4]  """)
    assertEquals(Seq(1, 2, 3, 4), conf.getList("a").unwrapped.asScala)
  }

  @Test
  def listConcatWithSubstitutions(): Unit = {
    val conf =
      parseConfig(""" a :  ${x} [3,4] ${y}, x : [1,2], y : [5,6]  """).resolve()
    assertEquals(Seq(1, 2, 3, 4, 5, 6), conf.getList("a").unwrapped.asScala)
  }

  @Test
  def listConcatSelfReferential(): Unit = {
    val conf =
      parseConfig(""" a : [1, 2], a : ${a} [3,4], a : ${a} [5,6]  """).resolve()
    assertEquals(Seq(1, 2, 3, 4, 5, 6), conf.getList("a").unwrapped.asScala)
  }

  @Test
  def noSubstitutionsListConcatCannotSpanLines(): Unit = {
    val e = intercept[ConfigException.Parse] {
      parseConfig(""" a :  [1,2]
                [3,4]  """)
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("expecting") &&
        e.getMessage.contains("'['")
    )
  }

  @Test
  def listConcatCanSpanLinesInsideBrackets(): Unit = {
    val conf = parseConfig(""" a :  [1,2
               ] [3,4]  """)
    assertEquals(Seq(1, 2, 3, 4), conf.getList("a").unwrapped.asScala)
  }

  @Test
  def noSubstitutionsObjectConcat(): Unit = {
    val conf = parseConfig(""" a : { b : c } { x : y }  """)
    assertEquals(
      Map("b" -> "c", "x" -> "y"),
      conf.getObject("a").unwrapped.asScala
    )
  }

  @Test
  def objectConcatMergeOrder(): Unit = {
    val conf = parseConfig(""" a : { b : 1 } { b : 2 } { b : 3 } { b : 4 } """)
    assertEquals(4, conf.getInt("a.b"))
  }

  @Test
  def objectConcatWithSubstitutions(): Unit = {
    val conf = parseConfig(
      """ a : ${x} { b : 1 } ${y}, x : { a : 0 }, y : { c : 2 } """
    ).resolve()
    assertEquals(
      Map("a" -> 0, "b" -> 1, "c" -> 2),
      conf.getObject("a").unwrapped.asScala
    )
  }

  @Test
  def objectConcatSelfReferential(): Unit = {
    val conf = parseConfig(
      """ a : { a : 0 }, a : ${a} { b : 1 }, a : ${a} { c : 2 } """
    ).resolve()
    assertEquals(
      Map("a" -> 0, "b" -> 1, "c" -> 2),
      conf.getObject("a").unwrapped.asScala
    )
  }

  @Test
  def objectConcatSelfReferentialOverride(): Unit = {
    val conf = parseConfig(""" a : { b : 3 }, a : { b : 2 } ${a} """).resolve()
    assertEquals(Map("b" -> 3), conf.getObject("a").unwrapped.asScala)
  }

  @Test
  def noSubstitutionsObjectConcatCannotSpanLines(): Unit = {
    val e = intercept[ConfigException.Parse] {
      parseConfig(""" a :  { b : c }
                    { x : y }""")
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("expecting") &&
        e.getMessage.contains("'{'")
    )
  }

  @Test
  def objectConcatCanSpanLinesInsideBraces(): Unit = {
    val conf = parseConfig(""" a :  { b : c
    } { x : y }  """)
    assertEquals(
      Map("b" -> "c", "x" -> "y"),
      conf.getObject("a").unwrapped.asScala
    )
  }

  @Test
  def stringConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ foo bar 10 ] """)
    assertEquals(Seq("foo bar 10"), conf.getStringList("a").asScala)
  }

  @Test
  def stringNonConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ foo
                bar
                10 ] """)
    assertEquals(Seq("foo", "bar", "10"), conf.getStringList("a").asScala)
  }

  @Test
  def objectConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ { b : c } { x : y } ] """)
    assertEquals(
      Seq(Map("b" -> "c", "x" -> "y")),
      conf.getObjectList("a").asScala.map(_.unwrapped.asScala)
    )
  }

  @Test
  def objectNonConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ { b : c }
                { x : y } ] """)
    assertEquals(
      Seq(Map("b" -> "c"), Map("x" -> "y")),
      conf.getObjectList("a").asScala.map(_.unwrapped.asScala)
    )
  }

  @Test
  def listConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ [1, 2] [3, 4] ] """)
    assertEquals(
      List(List(1, 2, 3, 4)),
      // well that's a little silly
      conf
        .getList("a")
        .unwrapped
        .asScala
        .toList
        .map(_.asInstanceOf[java.util.List[_]].asScala.toList)
    )
  }

  @Test
  def listNonConcatInsideArrayValue(): Unit = {
    val conf = parseConfig(""" a : [ [1, 2]
                [3, 4] ] """)
    assertEquals(
      List(List(1, 2), List(3, 4)),
      // well that's a little silly
      conf
        .getList("a")
        .unwrapped
        .asScala
        .toList
        .map(_.asInstanceOf[java.util.List[_]].asScala.toList)
    )
  }

  @Test
  def stringConcatsAreKeys(): Unit = {
    val conf = parseConfig(""" 123 foo : "value" """)
    assertEquals("value", conf.getString("123 foo"))
  }

  @Test
  def objectsAreNotKeys(): Unit = {
    val e = intercept[ConfigException.Parse] {
      parseConfig("""{ { a : 1 } : "value" }""")
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("expecting a close parentheses") && e.getMessage
        .contains("'{'")
    )
  }

  @Test
  def arraysAreNotKeys(): Unit = {
    val e = intercept[ConfigException.Parse] {
      parseConfig("""{ [ "a" ] : "value" }""")
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("expecting a close parentheses") && e.getMessage
        .contains("'['")
    )
  }

  @Test
  def emptyArrayPlusEquals(): Unit = {
    val conf = parseConfig(""" a = [], a += 2 """).resolve()
    assertEquals(Seq(2), conf.getIntList("a").asScala.toList)
  }

  @Test
  def missingArrayPlusEquals(): Unit = {
    val conf = parseConfig(""" a += 2 """).resolve()
    assertEquals(Seq(2), conf.getIntList("a").asScala.toList)
  }

  @Test
  def shortArrayPlusEquals(): Unit = {
    val conf = parseConfig(""" a = [1], a += 2 """).resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a").asScala.toList)
  }

  @Test
  def numberPlusEquals(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      val conf = parseConfig(""" a = 10, a += 2 """).resolve()
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("10") &&
        e.getMessage.contains("[2]")
    )
  }

  @Test
  def stringPlusEquals(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a = abc, a += 2 """).resolve()
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("abc") &&
        e.getMessage.contains("[2]")
    )
  }

  @Test
  def objectPlusEquals(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig(""" a = { x : y }, a += 2 """).resolve()
    }
    assertTrue(
      "wrong exception: " + e.getMessage,
      e.getMessage.contains("Cannot concatenate") &&
        e.getMessage.contains("\"x\":\"y\"") &&
        e.getMessage.contains("[2]")
    )
  }

  @Test
  def plusEqualsNestedPath(): Unit = {
    val conf = parseConfig(""" a.b.c = [1], a.b.c += 2 """).resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a.b.c").asScala.toList)
  }

  @Test
  def plusEqualsNestedObjects(): Unit = {
    val conf = parseConfig(
      """ a : { b : { c : [1] } }, a : { b : { c += 2 } }"""
    ).resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a.b.c").asScala.toList)
  }

  @Test
  def plusEqualsSingleNestedObject(): Unit = {
    val conf = parseConfig(""" a : { b : { c : [1], c += 2 } }""").resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a.b.c").asScala.toList)
  }

  @Test
  def substitutionPlusEqualsSubstitution(): Unit = {
    val conf =
      parseConfig(""" a = ${x}, a += ${y}, x = [1], y = 2 """).resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a").asScala.toList)
  }

  @Test
  def plusEqualsMultipleTimes(): Unit = {
    val conf = parseConfig(""" a += 1, a += 2, a += 3 """).resolve()
    assertEquals(Seq(1, 2, 3), conf.getIntList("a").asScala.toList)
  }

  @Test
  def plusEqualsMultipleTimesNested(): Unit = {
    val conf = parseConfig(""" x { a += 1, a += 2, a += 3 } """).resolve()
    assertEquals(Seq(1, 2, 3), conf.getIntList("x.a").asScala.toList)
  }

  @Test
  def plusEqualsAnObjectMultipleTimes(): Unit = {
    val conf =
      parseConfig(""" a += { b: 1 }, a += { b: 2 }, a += { b: 3 } """).resolve()
    assertEquals(
      Seq(1, 2, 3),
      conf.getObjectList("a").asScala.toList.map(_.toConfig.getInt("b"))
    )
  }

  @Test
  def plusEqualsAnObjectMultipleTimesNested(): Unit = {
    val conf = parseConfig(
      """ x { a += { b: 1 }, a += { b: 2 }, a += { b: 3 } } """
    ).resolve()
    assertEquals(
      Seq(1, 2, 3),
      conf.getObjectList("x.a").asScala.toList.map(_.toConfig.getInt("b"))
    )
  }

  // We would ideally make this case NOT throw an exception but we need to do some work
  // to get there, see https://github.com/lightbend/config/issues/160
  @Test
  def plusEqualsMultipleTimesNestedInArray(): Unit = {
    val e = intercept[ConfigException.Parse] {
      val conf =
        parseConfig("""x = [ { a += 1, a += 2, a += 3 } ] """).resolve()
      assertEquals(
        Seq(1, 2, 3),
        conf
          .getObjectList("x")
          .asScala
          .toVector(0)
          .toConfig
          .getIntList("a")
          .asScala
          .toList
      )
    }
    assertTrue(e.getMessage.contains("limitation"))
  }

  // We would ideally make this case NOT throw an exception but we need to do some work
  // to get there, see https://github.com/lightbend/config/issues/160
  @Test
  def plusEqualsMultipleTimesNestedInPlusEquals(): Unit = {
    val e = intercept[ConfigException.Parse] {
      val conf = parseConfig("""x += { a += 1, a += 2, a += 3 } """).resolve()
      assertEquals(
        Seq(1, 2, 3),
        conf
          .getObjectList("x")
          .asScala
          .toVector(0)
          .toConfig
          .getIntList("a")
          .asScala
          .toList
      )
    }
    assertTrue(e.getMessage.contains("limitation"))
  }

  // from https://github.com/lightbend/config/issues/177
  @Test
  def arrayConcatenationInDoubleNestedDelayedMerge(): Unit = {
    val unresolved = parseConfig(
      """d { x = [] }, c : ${d}, c { x += 1, x += 2 }"""
    )
    val conf = unresolved.resolve()
    assertEquals(Seq(1, 2), conf.getIntList("c.x").asScala)
  }

  // from https://github.com/lightbend/config/issues/177
  @Test
  def arrayConcatenationAsPartOfDelayedMerge(): Unit = {
    val unresolved = parseConfig(
      """ c { x: [], x : ${c.x}[1], x : ${c.x}[2] }"""
    )
    val conf = unresolved.resolve()
    assertEquals(Seq(1, 2), conf.getIntList("c.x").asScala)
  }

  // from https://github.com/lightbend/config/issues/177
  @Test
  def arrayConcatenationInDoubleNestedDelayedMerge2(): Unit = {
    val unresolved = parseConfig(
      """d { x = [] }, c : ${d}, c { x : ${c.x}[1], x : ${c.x}[2] }"""
    )
    val conf = unresolved.resolve()
    assertEquals(Seq(1, 2), conf.getIntList("c.x").asScala)
  }

  // from https://github.com/lightbend/config/issues/177
  @Test
  def arrayConcatenationInTripleNestedDelayedMerge(): Unit = {
    val unresolved = parseConfig(
      """{ r: { d.x=[] }, q: ${r}, q : { d { x = [] }, c : ${q.d}, c { x : ${q.c.x}[1], x : ${q.c.x}[2] } } }"""
    )
    val conf = unresolved.resolve()
    assertEquals(Seq(1, 2), conf.getIntList("q.c.x").asScala)
  }

  @Test
  def concatUndefinedSubstitutionWithString(): Unit = {
    val conf = parseConfig("""a = foo${?bar}""").resolve()
    assertEquals("foo", conf.getString("a"))
  }

  @Test
  def concatDefinedOptionalSubstitutionWithString(): Unit = {
    val conf = parseConfig("""bar=bar, a = foo${?bar}""").resolve()
    assertEquals("foobar", conf.getString("a"))
  }

  @Test
  def concatUndefinedSubstitutionWithArray(): Unit = {
    val conf = parseConfig("""a = [1] ${?bar}""").resolve()
    assertEquals(Seq(1), conf.getIntList("a").asScala.toList)
  }

  @Test
  def concatDefinedOptionalSubstitutionWithArray(): Unit = {
    val conf = parseConfig("""bar=[2], a = [1] ${?bar}""").resolve()
    assertEquals(Seq(1, 2), conf.getIntList("a").asScala.toList)
  }

  @Test
  def concatUndefinedSubstitutionWithObject(): Unit = {
    val conf = parseConfig("""a = { x : "foo" } ${?bar}""").resolve()
    assertEquals("foo", conf.getString("a.x"))
  }

  @Test
  def concatDefinedOptionalSubstitutionWithObject(): Unit = {
    val conf =
      parseConfig("""bar={ y : 42 }, a = { x : "foo" } ${?bar}""").resolve()
    assertEquals("foo", conf.getString("a.x"))
    assertEquals(42, conf.getInt("a.y"))
  }

  @Test
  def concatTwoUndefinedSubstitutions(): Unit = {
    val conf = parseConfig("""a = ${?foo}${?bar}""").resolve()
    assertFalse("no field 'a'", conf.hasPath("a"))
  }

  @Test
  def concatSeveralUndefinedSubstitutions(): Unit = {
    val conf = parseConfig("""a = ${?foo}${?bar}${?baz}${?woooo}""").resolve()
    assertFalse("no field 'a'", conf.hasPath("a"))
  }

  @Test
  def concatTwoUndefinedSubstitutionsWithASpace(): Unit = {
    val conf = parseConfig("""a = ${?foo} ${?bar}""").resolve()
    assertEquals(" ", conf.getString("a"))
  }

  @Test
  def concatTwoDefinedSubstitutionsWithASpace(): Unit = {
    val conf = parseConfig("""foo=abc, bar=def, a = ${foo} ${bar}""").resolve()
    assertEquals("abc def", conf.getString("a"))
  }

  @Test
  def concatTwoUndefinedSubstitutionsWithEmptyString(): Unit = {
    val conf = parseConfig("""a = ""${?foo}${?bar}""").resolve()
    assertEquals("", conf.getString("a"))
  }

  @Test
  def concatSubstitutionsThatAreObjectsWithNoSpace(): Unit = {
    val conf = parseConfig(
      """foo = { a : 1}, bar = { b : 2 }, x = ${foo}${bar}"""
    ).resolve()
    assertEquals(1, conf.getInt("x.a"))
    assertEquals(2, conf.getInt("x.b"))
  }

  // whitespace is insignificant if substitutions don't turn out to be a string
  @Test
  def concatSubstitutionsThatAreObjectsWithSpace(): Unit = {
    val conf = parseConfig(
      """foo = { a : 1}, bar = { b : 2 }, x = ${foo} ${bar}"""
    ).resolve()
    assertEquals(1, conf.getInt("x.a"))
    assertEquals(2, conf.getInt("x.b"))
  }

  // whitespace is insignificant if substitutions don't turn out to be a string
  @Test
  def concatSubstitutionsThatAreListsWithSpace(): Unit = {
    val conf =
      parseConfig("""foo = [1], bar = [2], x = ${foo} ${bar}""").resolve()
    assertEquals(List(1, 2), conf.getIntList("x").asScala)
  }

  // but quoted whitespace should be an error
  @Test
  def concatSubstitutionsThatAreObjectsWithQuotedSpace(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig("""foo = { a : 1}, bar = { b : 2 }, x = ${foo}"  "${bar}""")
        .resolve()
    }
  }

  // but quoted whitespace should be an error
  @Test
  def concatSubstitutionsThatAreListsWithQuotedSpace(): Unit = {
    val e = intercept[ConfigException.WrongType] {
      parseConfig("""foo = [1], bar = [2], x = ${foo}"  "${bar}""").resolve()
    }
  }
}
