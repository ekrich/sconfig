/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._

import org.ekrich.config.ConfigResolveOptions
import org.ekrich.config.ConfigFactory
import scala.jdk.CollectionConverters._

// remove checkNotSerializable so this can work for Scala.js
class ConfigSubstitutionTest extends TestUtils {
  private def resolveWithoutFallbacks(v: AbstractConfigObject) = {
    val options = ConfigResolveOptions.noSystem
    ResolveContext
      .resolve(v, v, options)
      .asInstanceOf[AbstractConfigObject]
      .toConfig
  }
  private def resolveWithoutFallbacks(
      s: AbstractConfigValue,
      root: AbstractConfigObject
  ) = {
    val options = ConfigResolveOptions.noSystem
    ResolveContext.resolve(s, root, options)
  }

  private def resolve(v: AbstractConfigObject) = {
    val options = ConfigResolveOptions.defaults
    ResolveContext
      .resolve(v, v, options)
      .asInstanceOf[AbstractConfigObject]
      .toConfig
  }
  private def resolve(s: AbstractConfigValue, root: AbstractConfigObject) = {
    val options = ConfigResolveOptions.defaults
    ResolveContext.resolve(s, root, options)
  }

  private val simpleObject = {
    parseObject("""
{
   "foo" : 42,
   "bar" : {
       "int" : 43,
       "bool" : true,
       "null" : null,
       "string" : "hello",
       "double" : 3.14
    }
}
""")
  }

  private val substComplexObject = {
    parseObject(
      """
{
    "foo" : ${bar},
    "bar" : ${a.b.c},
    "a" : { "b" : { "c" : 57, "d" : ${foo}, "e" : { "f" : ${foo} } } },
    "objA" : ${a},
    "objB" : ${a.b},
    "objE" : ${a.b.e},
    "foo.bar" : 37,
    "arr" : [ ${foo}, ${a.b.c}, ${"foo.bar"}, ${objB.d}, ${objA.b.e.f}, ${objE.f} ],
    "ptrToArr" : ${arr},
    "x" : { "y" : { "ptrToPtrToArr" : ${ptrToArr} } }
}
"""
    )
  }

  private val substSystemPropsObject =
    parseObject("""
      {
        "a" : ${configtest.a},
        "b" : ${configtest.b}
      }
      """)

  @Test
  def doNotSerializeUnresolvedObject(): Unit =
    checkNotSerializable(substComplexObject)

  @Test
  def resolveListFromSystemProps(): Unit = {
    val props = parseObject("""
                              |"a": ${testList}
                            """.stripMargin)

    System.setProperty("testList.0", "0")
    System.setProperty("testList.1", "1")
    ConfigImpl.reloadSystemPropertiesConfig()

    val resolved = resolve(
      ConfigFactory
        .systemProperties()
        .withFallback(props)
        .root
        .asInstanceOf[AbstractConfigObject]
    )

    assertEquals(List("0", "1"), resolved.getList("a").unwrapped.asScala)
  }

  @Test
  def resolveListFromEnvVars(): Unit = {
    val props = parseObject("""
                              |"a": ${testList}
                            """.stripMargin)

    // "testList.0" and "testList.1" are defined as envVars in build.sbt
    val resolved = resolve(props)

    assertEquals(List("0", "1"), resolved.getList("a").unwrapped.asScala)
  }

  // this is a weird test, it used to test fallback to system props which made more sense.
  // Now it just tests that if you override with system props, you can use system props
  // in substitutions.
  @Test
  def overrideWithSystemProps(): Unit = {
    System.setProperty("configtest.a", "1234")
    System.setProperty("configtest.b", "5678")
    ConfigImpl.reloadSystemPropertiesConfig()

    val resolved = resolve(
      ConfigFactory
        .systemProperties()
        .withFallback(substSystemPropsObject)
        .root
        .asInstanceOf[AbstractConfigObject]
    )

    assertEquals("1234", resolved.getString("a"))
    assertEquals("5678", resolved.getString("b"))
  }

  private val substEnvVarObject = {
    // prefix the names of keys with "key_" to allow us to embed a case sensitive env var name
    // in the key that wont therefore risk a naming collision with env vars themselves
    parseObject(
      """
{
    "key_HOME" : ${?HOME},
    "key_PWD" : ${?PWD},
    "key_SHELL" : ${?SHELL},
    "key_LANG" : ${?LANG},
    "key_PATH" : ${?PATH},
    "key_Path" : ${?Path}, // many windows machines use Path rather than PATH
    "key_NOT_HERE" : ${?NOT_HERE}
}
"""
    )
  }

  @Test
  def fallbackToEnv(): Unit = {
    val resolved = resolve(substEnvVarObject)

    var existed = 0
    for (k <- resolved.root.keySet().asScala) {
      val envVarName = k.replace("key_", "")
      val e = System.getenv(envVarName)
      if (e != null) {
        existed += 1
        assertEquals(e, resolved.getString(k))
      } else {
        assertNull(resolved.root.get(k))
      }
    }
    if (existed == 0) {
      throw new Exception(
        "None of the env vars we tried to use for testing were set"
      )
    }
  }

  @Test
  def fallbackToEnvWhenRelativized(): Unit = {
    val values = new java.util.HashMap[String, AbstractConfigValue]()

    values.put("a", substEnvVarObject.relativized(new Path("a")))

    val resolved = resolve(new SimpleConfigObject(fakeOrigin(), values))

    var existed = 0
    for (k <- resolved.getObject("a").keySet().asScala) {
      val envVarName = k.replace("key_", "")
      val e = System.getenv(envVarName)
      if (e != null) {
        existed += 1
        assertEquals(e, resolved.getConfig("a").getString(k))
      } else {
        assertNull(resolved.getObject("a").get(k))
      }
    }
    if (existed == 0) {
      throw new Exception(
        "None of the env vars we tried to use for testing were set"
      )
    }
  }
}
