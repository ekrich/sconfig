package org.ekrich.config.impl

import org.junit.Assert.*
import org.junit.*

import scala.jdk.CollectionConverters.*

import org.ekrich.config.Config
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigList
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigRenderOptions
import org.ekrich.config.ConfigValue

// Not from lightbend/config: pins that parsing generated HOCON either succeeds or throws
// ConfigException, that rendering is a parse/reparse fixed point, and that origin line
// numbers stay inside the input (deterministic core of draft PR #642, made sconfig-only).
class HoconPropertyTest extends TestUtilsShared {

  private val Seed = 20261005L

  private val renderOptions = ConfigRenderOptions.defaults
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)
    .setFormatted(true)

  @Test
  def parseSucceedsOrThrowsConfigException(): Unit = {
    val gen = new DocGen(new scala.util.Random(Seed))
    for (_ <- 1 to 400) {
      val input = gen.doc(chaos = true, deep = gen.int(25) == 0)
      try ConfigFactory.parseString(input)
      catch {
        case _: ConfigException => ()
        case t: Throwable       =>
          fail(
            "parse escaped with " + t.getClass.getName + ": " + t.getMessage +
              "\ninput: " + show(input)
          )
      }
    }
  }

  @Test
  def renderedTextIsAFixedPoint(): Unit = {
    val gen = new DocGen(new scala.util.Random(Seed + 1))
    var done = 0
    var attempts = 0
    while (done < 300 && attempts < 3000) {
      attempts += 1
      val input = gen.doc(chaos = false, deep = gen.int(25) == 0)
      var config: Config = null
      try config = ConfigFactory.parseString(input)
      catch { case _: ConfigException => () }
      if (config != null) {
        done += 1
        val r1 = config.root.render(renderOptions)
        var reparsed: Config = null
        try reparsed = ConfigFactory.parseString(r1)
        catch {
          case e: ConfigException =>
            fail(
              "rendered text does not re-parse: " + e.getMessage +
                "\nrendered: " + show(r1) + "\ninput: " + show(input)
            )
        }
        val r2 = reparsed.root.render(renderOptions)
        assertEquals(
          "render is not a fixed point\ninput: " + show(
            input
          ) + "\nr1: " + show(r1) +
            "\nr2: " + show(r2),
          r1,
          r2
        )
        try checkEqualObjects(config, reparsed)
        catch {
          case e: AssertionError =>
            throw new AssertionError(
              "parses differ\ninput: " + show(input) + "\nr1: " + show(r1),
              e
            )
        }
      }
    }
    assertEquals(300, done)
  }

  @Test
  def originLinesStayInsideInput(): Unit = {
    val gen = new DocGen(new scala.util.Random(Seed + 2))
    var done = 0
    var attempts = 0
    while (done < 300 && attempts < 3000) {
      attempts += 1
      val input = gen.doc(chaos = false, deep = gen.int(25) == 0)
      var config: Config = null
      try config = ConfigFactory.parseString(input)
      catch { case _: ConfigException => () }
      if (config != null) {
        done += 1
        checkOriginLines(config.root, lineCount(input), input)
      }
    }
    assertEquals(300, done)
  }

  private def checkOriginLines(
      value: ConfigValue,
      max: Int,
      input: String
  ): Unit = {
    val line = value.origin.lineNumber
    assertTrue(
      "origin line " + line + " outside 1.." + max + "\ninput: " + show(input),
      line >= 1 && line <= max
    )
    value match {
      case o: ConfigDelayedMergeObject =>
        o.unmergedValues.asScala.foreach(v => checkOriginLines(v, max, input))
      case m: ConfigDelayedMerge =>
        m.unmergedValues.asScala.foreach(v => checkOriginLines(v, max, input))
      case o: ConfigObject =>
        o.entrySet.asScala.foreach(e =>
          checkOriginLines(e.getValue, max, input)
        )
      case l: ConfigList =>
        l.asScala.foreach(v => checkOriginLines(v, max, input))
      case _ => ()
    }
  }

  private def lineCount(s: String): Int = {
    var n = 1
    var i = 0
    while (i < s.length) {
      if (s.charAt(i) == '\n') n += 1
      i += 1
    }
    if (s.endsWith("\n")) n - 1 else n
  }

  private def show(s: String): String =
    s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

  private final class DocGen(random: scala.util.Random) {
    def int(bound: Int): Int = random.nextInt(bound)

    def between(lo: Int, hi: Int): Int = lo + int(hi - lo + 1)

    def oneOf[T](xs: Vector[T]): T = xs(int(xs.length))

    def pick[T](weighted: (Int, () => T)*): T = {
      var n = int(weighted.map(_._1).sum)
      var result: T = null.asInstanceOf[T]
      val it = weighted.iterator
      var chosen = false
      while (!chosen) {
        val (w, gen) = it.next()
        if (n < w) {
          result = gen()
          chosen = true
        } else n -= w
      }
      result
    }

    def doc(chaos: Boolean, deep: Boolean): String =
      if (deep) deepDoc(chaos)
      else {
        val n = between(1, 4)
        val lines = (1 to n).map { i =>
          val e = entry(if (int(3) == 0) between(1, 3) else 0, chaos)
          if (i < n && int(5) == 0) e + "\n" + oneOf(noise) else e
        }
        val body = lines.mkString("\n")
        val wrapped = if (int(7) == 0) "{\n" + body + "\n}" else body
        wrapped + (if (int(3) == 0) "\n" else "")
      }

    private def deepDoc(chaos: Boolean): String = {
      val d = between(10, 30)
      def nest(i: Int): String =
        if (i == 0) atom(chaos)
        else if (i % 3 == 0) "[{ k " + nest(i - 1) + " }]"
        else "{ k " + nest(i - 1) + " }"
      "root " + nest(d)
    }

    private def entry(d: Int, chaos: Boolean): String = {
      val pre = if (int(7) == 0) oneOf(Vector("# c\n", "// c\n")) else ""
      val post = if (int(9) == 0) oneOf(Vector(" # t", " // t")) else ""
      val s = if (int(12) == 0) "+=" else sep
      val v = if (s == "+=") atom(chaos) else value(d, chaos)
      pre + pathKey(chaos) + s + v + post
    }

    private def value(d: Int, chaos: Boolean): String =
      if (d <= 0) atom(chaos)
      else
        pick(
          (6, () => atom(chaos)),
          (2, () => array(d - 1, chaos)),
          (2, () => obj(d - 1, chaos)),
          (1, () => concat(d - 1, chaos))
        )

    private def array(d: Int, chaos: Boolean): String = {
      val n = between(1, 4)
      val items =
        (1 to n)
          .map(_ => value(d, chaos))
          .mkString(oneOf(Vector(", ", ",", ",\n")))
      "[" + items + (if (int(10) == 0) "," else "") + "]"
    }

    private def obj(d: Int, chaos: Boolean): String = {
      val n = between(1, 4)
      val entries =
        (1 to n)
          .map(_ => entry(d, chaos))
          .mkString(oneOf(Vector("\n", ", ", " ")))
      "{ " + entries + " }"
    }

    private def concat(d: Int, chaos: Boolean): String = {
      val n = between(2, 3)
      (1 to n).map(_ => value(d, chaos)).mkString(" ")
    }

    private def atom(chaos: Boolean): String =
      pick(
        (4, () => quotedString(chaos)),
        (2, () => unquoted(chaos)),
        (1, () => multilineString(chaos)),
        (2, () => substitution)
      )

    private def sep: String =
      oneOf(Vector("=", ":", "= ", ": ", " = ", " : ", "=", ":"))

    private def pathKey(chaos: Boolean): String = {
      val n = between(1, 3)
      (1 to n).map(_ => key(chaos)).mkString(".")
    }

    private def key(chaos: Boolean): String =
      pick(
        (6, () => oneOf(simpleKeys)),
        (2, () => "\"" + oneOf(quotedKeys) + "\""),
        (if (chaos) 2 else 0, () => oneOf(weirdKeys))
      )

    private def substitution: String = {
      val p = oneOf(subPaths)
      val opt = if (int(4) == 0) "?" else ""
      val expansion = if (int(10) == 0) "[]" else ""
      val ws = if (int(8) == 0) " " else ""
      if (p.length == 1 && expansion.isEmpty && int(8) == 0) "$" + p
      else "${" + ws + opt + p + expansion + ws + "}"
    }

    private def quotedString(chaos: Boolean): String = {
      var body = "x"
      val n = between(1, 3)
      for (_ <- 1 to n) {
        body += pick(
          (5, () => oneOf(validEscapes)),
          (2, () => hexEscape),
          (2, () => oneOf(rawBitsStrict)),
          (if (chaos) 2 else 0, () => oneOf(invalidEscapes)),
          (if (chaos) 2 else 0, () => oneOf(rawBitsChaos))
        )
      }
      "\"" + body + "y\""
    }

    private def multilineString(chaos: Boolean): String =
      "\"\"\"" + oneOf(
        if (chaos) multilineChaos else multilineStrict
      ) + "\"\"\""

    private def unquoted(chaos: Boolean): String =
      pick(
        (3, () => oneOf(strictWords)),
        (3, () => oneOf(if (chaos) numbers else numbersSafe)),
        (1, () => oneOf(durationNumbers) + oneOf(durationUnits)),
        (1, () => oneOf(sizeNumbers) + oneOf(sizeUnits)),
        (if (chaos) 2 else 0, () => oneOf(chaosWords))
      )

    private def hexEscape: String = {
      val hex = "0123456789abcdefABCDEF"
      var e = "\\u"
      for (_ <- 1 to 4) e += hex(int(22))
      e
    }

    private val simpleKeys = Vector(
      "a",
      "b",
      "foo",
      "bar",
      "key",
      "k0",
      "x_1",
      "A-B",
      "0",
      "00",
      "-x",
      "true",
      "null",
      "on"
    )

    private val weirdKeys = Vector(
      "with space",
      "Ünïcödé",
      "1.5",
      "%pct",
      "a:b",
      "#no",
      "/slash",
      "'q'",
      "@at",
      "+plus",
      "[br",
      "]",
      "}",
      "{",
      ",",
      "=",
      "$dollar",
      "star*",
      "?q",
      "!bang",
      "end.",
      "a.b.c",
      "back\\slash",
      "quo\"te",
      "tab\tin"
    )

    private val quotedKeys = Vector(
      "a",
      "b.c",
      "with space",
      "",
      "1",
      "\\u0041",
      "a\\nb",
      "esc\\\" q",
      "back\\\\slash"
    )

    private val subPaths = Vector("a", "b", "x", "a.b", "c.d.e", "missing")

    private val validEscapes = Vector(
      "\\n",
      "\\t",
      "\\r",
      "\\\"",
      "\\\\",
      "\\/",
      "\\b",
      "\\f",
      "\\u0041",
      "\\u00e9",
      "\\uD83D\\uDE00",
      "\\u0000",
      "\\u007f",
      "\\uFEFF"
    )

    private val invalidEscapes = Vector(
      "\\q",
      "\\x41",
      "\\u12",
      "\\uGGGG",
      "\\U0041",
      "\\ ",
      "\\'",
      "\\e"
    )

    private val rawBitsStrict = Vector(
      "\"\"",
      "\t",
      " ",
      "é",
      "😀",
      " ",
      "${x}",
      "$x",
      "//",
      "#",
      "''",
      "\\n",
      "\\u0041"
    )

    private val rawBitsChaos = Vector(
      "\"",
      "\n",
      "\r",
      "\r\n",
      "",
      "\u0007",
      "}",
      "{",
      "${}",
      "\\\\"
    )

    private val multilineStrict = Vector(
      "",
      "x",
      "a\nb",
      "\"quotes\" inside",
      "back\\slash",
      "end\\\nnext",
      "  indented\n  more"
    )

    private val multilineChaos = Vector(
      "",
      "x",
      "a\nb",
      "\"",
      "\"\"",
      "\"\"\"",
      "a\"b",
      "\\",
      "x\u0000y",
      "a\\",
      "trailing\\"
    )

    private val strictWords = Vector(
      "true",
      "false",
      "null",
      "hello",
      "a-b",
      "on",
      "yes",
      "undefined"
    )

    private val chaosWords = Vector(
      "=",
      ":",
      "{",
      "}",
      "[",
      "]",
      ",",
      "+",
      "#",
      "\\\\",
      "\"a\"",
      "$",
      "${",
      "a}",
      "%",
      "|",
      "(",
      ")",
      "..",
      "00",
      "0x"
    )

    private val numbers = Vector(
      "0",
      "1",
      "-1",
      "+1",
      "007",
      "0.5",
      ".5",
      "5.",
      "1e3",
      "1E3",
      "1e+3",
      "1e-3",
      "1e",
      "1.2.3",
      "0xFF",
      "0XFF",
      "0x",
      "-0x10",
      "0o17",
      "010",
      "0b1010",
      "9223372036854775807",
      "9223372036854775808",
      "-9223372036854775808",
      "99999999999999999999999999",
      "1e1000",
      "-1e1000",
      "0.0",
      "-0.0",
      "1.7976931348623157E308",
      "4.9E-324",
      "NaN",
      "Infinity",
      "-Infinity",
      "1.5e",
      "1.5e3.5",
      "1_000"
    )

    private val numbersSafe =
      numbers.filterNot(t => t == "1e1000" || t == "-1e1000")

    private val durationNumbers = Vector(
      "0",
      "1",
      "10",
      "-5",
      "+3",
      "1.5",
      "1e2",
      "99999999999999999999999"
    )

    private val durationUnits = Vector(
      "ns",
      "us",
      "ms",
      "s",
      "m",
      "h",
      "d",
      "S",
      "MS",
      "seconds",
      " s",
      "  ms "
    )

    private val sizeNumbers = Vector(
      "0",
      "1",
      "10",
      "-1",
      "1.5",
      "1000000",
      "1e2"
    )

    private val sizeUnits = Vector(
      "B",
      "b",
      "kB",
      "KB",
      "K",
      "k",
      "MB",
      "M",
      "m",
      "GB",
      "G",
      "TB",
      "T",
      "PB",
      "P",
      "EB",
      "E",
      "kB ",
      " B",
      "kiB"
    )

    private val noise = Vector(
      "#",
      "//",
      "# include \"a.conf\"",
      "// include required(\"b.conf\")",
      "",
      "   ",
      "\t",
      "# noise"
    )
  }
}
