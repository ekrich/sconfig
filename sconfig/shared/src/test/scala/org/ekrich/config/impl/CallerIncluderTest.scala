package org.ekrich.config.impl

import java.io.File

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config._

/**
 * A caller-supplied [[ConfigIncluder]] resolves include statements on every
 * platform, including a Scala.js program in a browser and a Scala Native
 * program.
 *
 * The includer below answers include names from a map of text the caller
 * supplies. `docs/SCALA_NATIVE.md` shows the same class as the recommended way
 * to use includes where the library itself does no IO.
 */
class CallerIncluderTest {
  private class MapIncluder(entries: Map[String, String])
      extends ConfigIncluder
      with ConfigIncluderFile
      with ConfigIncluderClasspath {

    override def withFallback(fallback: ConfigIncluder): ConfigIncluder = this

    override def include(
        context: ConfigIncludeContext,
        what: String
    ): ConfigObject =
      resolve(context, what)

    override def includeFile(
        context: ConfigIncludeContext,
        what: File
    ): ConfigObject =
      resolve(context, what.getPath())

    override def includeResources(
        context: ConfigIncludeContext,
        what: String
    ): ConfigObject =
      resolve(context, what)

    private def resolve(
        context: ConfigIncludeContext,
        name: String
    ): ConfigObject =
      textFor(name) match {
        case Some(text) =>
          ConfigFactory.parseString(text, context.parseOptions).root
        case None =>
          if (context.parseOptions.getAllowMissing) {
            ConfigFactory.empty.root
          } else {
            throw new ConfigException.IO(
              ConfigFactory.empty.origin,
              "include was not found: '" + name + "'"
            )
          }
      }

    private def textFor(name: String): Option[String] = {
      val candidates =
        if (hasExtension(name)) List(name)
        else List(name + ".conf", name + ".json", name + ".properties")
      candidates.collectFirst {
        case candidate if entries.contains(candidate) => entries(candidate)
      }
    }

    private def hasExtension(name: String): Boolean =
      name.endsWith(".conf") || name.endsWith(".json") ||
        name.endsWith(".properties")
  }

  private def map(entries: (String, String)*): ConfigIncluder =
    new MapIncluder(entries.toMap)

  private def parse(text: String, includer: ConfigIncluder): Config =
    ConfigFactory.parseString(
      text,
      ConfigParseOptions.defaults.setIncluder(includer)
    )

  @Test
  def heuristicInclude(): Unit = {
    val config =
      parse("include \"base.conf\"\na = 1\n", map("base.conf" -> "b = 2\n"))
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
  }

  @Test
  def heuristicIncludeWithoutExtension(): Unit = {
    val config =
      parse("include \"base\"\na = 1\n", map("base.conf" -> "b = 2\n"))
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
  }

  @Test
  def fileInclude(): Unit = {
    val config =
      parse(
        "include file(\"base.conf\")\na = 1\n",
        map("base.conf" -> "b = 2\n")
      )
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
  }

  @Test
  def classpathInclude(): Unit = {
    val config = parse(
      "include classpath(\"base.conf\")\na = 1\n",
      map("base.conf" -> "b = 2\n")
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
  }

  @Test
  def nestedInclude(): Unit = {
    val config = parse(
      "include \"base.conf\"\na = 1\n",
      map(
        "base.conf" -> "include \"deep.conf\"\nb = 2\n",
        "deep.conf" -> "d = 4\n"
      )
    )
    assertEquals(1, config.getInt("a"))
    assertEquals(2, config.getInt("b"))
    assertEquals(4, config.getInt("d"))
  }

  @Test
  def missingIncludeIsIgnored(): Unit = {
    val config = parse("include \"nothere.conf\"\na = 1\n", map())
    assertEquals(1, config.getInt("a"))
  }

  @Test
  def requiredMissingIncludeFails(): Unit = {
    try {
      parse("include required(\"nothere.conf\")\na = 1\n", map())
      fail("expected a ConfigException for a required missing include")
    } catch {
      case _: ConfigException => ()
    }
  }

  @Test
  def includeOverridesEarlierValue(): Unit = {
    val config =
      parse("a = 1\ninclude \"base.conf\"\n", map("base.conf" -> "a = 2\n"))
    assertEquals(2, config.getInt("a"))
  }
}
