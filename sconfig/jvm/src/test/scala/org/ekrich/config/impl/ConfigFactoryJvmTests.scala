package org.ekrich.config.impl

import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.{
  DAYS,
  HOURS,
  MICROSECONDS,
  MILLISECONDS,
  MINUTES,
  NANOSECONDS,
  SECONDS
}

import scala.jdk.CollectionConverters._

import org.junit.Assert._
import org.junit.Test

import FileUtils._

import org.ekrich.config._

class ConfigFactoryJvmTests extends TestUtils {
  @Test
  def test01Getting(): Unit = {
    val conf = ConfigFactory.load("test01")

    // get all the primitive types
    assertEquals(42, conf.getInt("ints.fortyTwo"))
    assertEquals(42, conf.getInt("ints.fortyTwoAgain"))
    assertEquals(42L, conf.getLong("ints.fortyTwoAgain"))
    assertEquals(42.1, conf.getDouble("floats.fortyTwoPointOne"), 1e-6)
    assertEquals(42.1, conf.getDouble("floats.fortyTwoPointOneAgain"), 1e-6)
    assertEquals(0.33, conf.getDouble("floats.pointThirtyThree"), 1e-6)
    assertEquals(0.33, conf.getDouble("floats.pointThirtyThreeAgain"), 1e-6)
    assertEquals("abcd", conf.getString("strings.abcd"))
    assertEquals("abcd", conf.getString("strings.abcdAgain"))
    assertEquals(
      "null bar 42 baz true 3.14 hi",
      conf.getString("strings.concatenated")
    )
    assertEquals(true, conf.getBoolean("booleans.trueAgain"))
    assertEquals(false, conf.getBoolean("booleans.falseAgain"))

    // to get null we have to use the get() method from Map,
    // which takes a key and not a path
    assertEquals(nullValue(), conf.getObject("nulls").get("null"))
    assertNull(conf.root.get("notinthefile"))

    // get stuff with getValue
    assertEquals(intValue(42), conf.getValue("ints.fortyTwo"))
    assertEquals(stringValue("abcd"), conf.getValue("strings.abcd"))

    // get stuff with getAny
    assertEquals(42, conf.getAnyRef("ints.fortyTwo"))
    assertEquals("abcd", conf.getAnyRef("strings.abcd"))
    assertEquals(false, conf.getAnyRef("booleans.falseAgain"))

    // get empty array as any type of array
    assertEquals(Seq(), conf.getAnyRefList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getIntList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getLongList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getStringList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getLongList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getDoubleList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getObjectList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getBooleanList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getNumberList("arrays.empty").asScala)
    assertEquals(Seq(), conf.getList("arrays.empty").asScala)

    // get typed arrays
    assertEquals(Seq(1, 2, 3), conf.getIntList("arrays.ofInt").asScala)
    assertEquals(Seq(1L, 2L, 3L), conf.getLongList("arrays.ofInt").asScala)
    assertEquals(
      Seq("a", "b", "c"),
      conf.getStringList("arrays.ofString").asScala
    )
    assertEquals(
      Seq(3.14, 4.14, 5.14),
      conf.getDoubleList("arrays.ofDouble").asScala
    )
    assertEquals(
      Seq(null, null, null),
      conf.getAnyRefList("arrays.ofNull").asScala
    )
    assertEquals(
      Seq(true, false),
      conf.getBooleanList("arrays.ofBoolean").asScala
    )
    val listOfLists = conf.getAnyRefList("arrays.ofArray").asScala map {
      _.asInstanceOf[java.util.List[_]].asScala
    }
    assertEquals(
      Seq(Seq("a", "b", "c"), Seq("a", "b", "c"), Seq("a", "b", "c")),
      listOfLists
    )
    assertEquals(3, conf.getObjectList("arrays.ofObject").asScala.length)

    assertEquals(
      Seq("a", "b"),
      conf.getStringList("arrays.firstElementNotASubst").asScala
    )

    // plain getList should work
    assertEquals(
      Seq(intValue(1), intValue(2), intValue(3)),
      conf.getList("arrays.ofInt").asScala
    )
    assertEquals(
      Seq(stringValue("a"), stringValue("b"), stringValue("c")),
      conf.getList("arrays.ofString").asScala
    )

    // make sure floats starting with a '.' are parsed as strings (they will be converted to double on demand)
    assertEquals(
      ConfigValueType.STRING,
      conf.getValue("floats.pointThirtyThree").valueType
    )
  }

  @Test
  def test01Exceptions(): Unit = {
    val conf = ConfigFactory.load("test01")

    // should throw Missing if key doesn't exist
    intercept[ConfigException.Missing] {
      conf.getInt("doesnotexist")
    }

    // should throw Null if key is null
    intercept[ConfigException.Null] {
      conf.getInt("nulls.null")
    }

    intercept[ConfigException.Null] {
      conf.getIntList("nulls.null")
    }

    intercept[ConfigException.Null] {
      conf.getDuration("nulls.null", TimeUnit.MILLISECONDS)
    }

    intercept[ConfigException.Null] {
      conf.getDuration("nulls.null", TimeUnit.NANOSECONDS)
    }

    intercept[ConfigException.Null] {
      conf.getBytes("nulls.null")
    }

    intercept[ConfigException.Null] {
      conf.getMemorySize("nulls.null")
    }

    // should throw WrongType if key is wrong type and not convertible
    intercept[ConfigException.WrongType] {
      conf.getInt("booleans.trueAgain")
    }

    intercept[ConfigException.WrongType] {
      conf.getBooleanList("arrays.ofInt")
    }

    intercept[ConfigException.WrongType] {
      conf.getIntList("arrays.ofBoolean")
    }

    intercept[ConfigException.WrongType] {
      conf.getObjectList("arrays.ofInt")
    }

    intercept[ConfigException.WrongType] {
      conf.getDuration("ints", TimeUnit.MILLISECONDS)
    }

    intercept[ConfigException.WrongType] {
      conf.getDuration("ints", TimeUnit.NANOSECONDS)
    }

    intercept[ConfigException.WrongType] {
      conf.getBytes("ints")
    }

    intercept[ConfigException.WrongType] {
      conf.getMemorySize("ints")
    }

    // should throw BadPath on various bad paths
    intercept[ConfigException.BadPath] {
      conf.getInt(".bad")
    }

    intercept[ConfigException.BadPath] {
      conf.getInt("bad.")
    }

    intercept[ConfigException.BadPath] {
      conf.getInt("bad..bad")
    }

    // should throw BadValue on things that don't parse
    // as durations and sizes
    intercept[ConfigException.BadValue] {
      conf.getDuration("strings.a", TimeUnit.MILLISECONDS)
    }

    intercept[ConfigException.BadValue] {
      conf.getDuration("strings.a", TimeUnit.NANOSECONDS)
    }

    intercept[ConfigException.BadValue] {
      conf.getBytes("strings.a")
    }

    intercept[ConfigException.BadValue] {
      conf.getMemorySize("strings.a")
    }
  }

  @Test
  def test01Conversions(): Unit = {
    val conf = ConfigFactory.load("test01")

    // should convert numbers to string
    assertEquals("42", conf.getString("ints.fortyTwo"))
    assertEquals("42.1", conf.getString("floats.fortyTwoPointOne"))
    assertEquals(".33", conf.getString("floats.pointThirtyThree"))

    // should convert string to number
    assertEquals(57, conf.getInt("strings.number"))
    assertEquals(3.14, conf.getDouble("strings.double"), 1e-6)
    assertEquals(0.33, conf.getDouble("strings.doubleStartingWithDot"), 1e-6)

    // should convert strings to boolean
    assertEquals(true, conf.getBoolean("strings.true"))
    assertEquals(true, conf.getBoolean("strings.yes"))
    assertEquals(false, conf.getBoolean("strings.false"))
    assertEquals(false, conf.getBoolean("strings.no"))

    // converting some random string to boolean fails though
    intercept[ConfigException.WrongType] {
      conf.getBoolean("strings.abcd")
    }

    // FIXME test convert string "null" to a null value

    // should not convert strings to object or list
    intercept[ConfigException.WrongType] {
      conf.getObject("strings.a")
    }

    intercept[ConfigException.WrongType] {
      conf.getList("strings.a")
    }

    // should not convert object or list to string
    intercept[ConfigException.WrongType] {
      conf.getString("ints")
    }

    intercept[ConfigException.WrongType] {
      conf.getString("arrays.ofInt")
    }

    // should get durations
    def asNanos(secs: Int) = TimeUnit.SECONDS.toNanos(secs.toLong)
    assertEquals(
      1000L,
      conf.getDuration("durations.second", TimeUnit.MILLISECONDS)
    )
    assertEquals(
      asNanos(1),
      conf.getDuration("durations.second", TimeUnit.NANOSECONDS)
    )
    assertEquals(
      1000L,
      conf.getDuration("durations.secondAsNumber", TimeUnit.MILLISECONDS)
    )
    assertEquals(
      asNanos(1),
      conf.getDuration("durations.secondAsNumber", TimeUnit.NANOSECONDS)
    )
    assertEquals(
      Seq(1000L, 2000L, 3000L, 4000L),
      conf
        .getDurationList("durations.secondsList", TimeUnit.MILLISECONDS)
        .asScala
    )
    assertEquals(
      Seq(asNanos(1), asNanos(2), asNanos(3), asNanos(4)),
      conf
        .getDurationList("durations.secondsList", TimeUnit.NANOSECONDS)
        .asScala
    )
    assertEquals(
      500L,
      conf.getDuration("durations.halfSecond", TimeUnit.MILLISECONDS)
    )
    assertEquals(
      4878955355435272204L,
      conf.getDuration("durations.largeNanos", TimeUnit.NANOSECONDS)
    )
    assertEquals(
      4878955355435272204L,
      conf.getDuration("durations.plusLargeNanos", TimeUnit.NANOSECONDS)
    )
    assertEquals(
      -4878955355435272204L,
      conf.getDuration("durations.minusLargeNanos", TimeUnit.NANOSECONDS)
    )

    // get durations as java.time.Duration
    assertEquals(1000L, conf.getDuration("durations.second").toMillis)
    assertEquals(asNanos(1), conf.getDuration("durations.second").toNanos)
    assertEquals(1000L, conf.getDuration("durations.secondAsNumber").toMillis)
    assertEquals(
      asNanos(1),
      conf.getDuration("durations.secondAsNumber").toNanos
    )
    assertEquals(
      Seq(1000L, 2000L, 3000L, 4000L),
      conf.getDurationList("durations.secondsList").asScala.map(_.toMillis)
    )
    assertEquals(
      Seq(asNanos(1), asNanos(2), asNanos(3), asNanos(4)),
      conf.getDurationList("durations.secondsList").asScala.map(_.toNanos)
    )
    assertEquals(500L, conf.getDuration("durations.halfSecond").toMillis)
    assertEquals(
      4878955355435272204L,
      conf.getDuration("durations.largeNanos").toNanos
    )
    assertEquals(
      4878955355435272204L,
      conf.getDuration("durations.plusLargeNanos").toNanos
    )
    assertEquals(
      -4878955355435272204L,
      conf.getDuration("durations.minusLargeNanos").toNanos
    )

    def assertDurationAsTimeUnit(unit: TimeUnit): Unit = {
      def ns2unit(l: Long) = unit.convert(l, NANOSECONDS)
      def ms2unit(l: Long) = unit.convert(l, MILLISECONDS)
      def s2unit(i: Int) = unit.convert(i.toLong, SECONDS)
      assertEquals(ms2unit(1000L), conf.getDuration("durations.second", unit))
      assertEquals(s2unit(1), conf.getDuration("durations.second", unit))
      assertEquals(
        ms2unit(1000L),
        conf.getDuration("durations.secondAsNumber", unit)
      )
      assertEquals(
        s2unit(1),
        conf.getDuration("durations.secondAsNumber", unit)
      )
      assertEquals(
        Seq(1000L, 2000L, 3000L, 4000L) map ms2unit,
        conf.getDurationList("durations.secondsList", unit).asScala
      )
      assertEquals(
        Seq(1, 2, 3, 4) map s2unit,
        conf.getDurationList("durations.secondsList", unit).asScala
      )
      assertEquals(
        ms2unit(500L),
        conf.getDuration("durations.halfSecond", unit)
      )
      assertEquals(ms2unit(1L), conf.getDuration("durations.millis", unit))
      assertEquals(ms2unit(2L), conf.getDuration("durations.micros", unit))
      assertEquals(
        ns2unit(4878955355435272204L),
        conf.getDuration("durations.largeNanos", unit)
      )
      assertEquals(
        ns2unit(4878955355435272204L),
        conf.getDuration("durations.plusLargeNanos", unit)
      )
      assertEquals(
        ns2unit(-4878955355435272204L),
        conf.getDuration("durations.minusLargeNanos", unit)
      )
    }

    assertDurationAsTimeUnit(NANOSECONDS)
    assertDurationAsTimeUnit(MICROSECONDS)
    assertDurationAsTimeUnit(MILLISECONDS)
    assertDurationAsTimeUnit(SECONDS)
    assertDurationAsTimeUnit(MINUTES)
    assertDurationAsTimeUnit(HOURS)
    assertDurationAsTimeUnit(DAYS)

    // periods
    assertEquals(1, conf.getPeriod("periods.day").get(ChronoUnit.DAYS))
    assertEquals(2, conf.getPeriod("periods.dayAsNumber").getDays)
    assertEquals(3 * 7, conf.getTemporal("periods.week").get(ChronoUnit.DAYS))
    assertEquals(5, conf.getTemporal("periods.month").get(ChronoUnit.MONTHS))
    assertEquals(8, conf.getTemporal("periods.year").get(ChronoUnit.YEARS))

    // should get size in bytes
    assertEquals(1024 * 1024L, conf.getBytes("memsizes.meg"))
    assertEquals(1024 * 1024L, conf.getBytes("memsizes.megAsNumber"))
    assertEquals(
      Seq(1024 * 1024L, 1024 * 1024L, 1024L * 1024L),
      conf.getBytesList("memsizes.megsList").asScala
    )
    assertEquals(512 * 1024L, conf.getBytes("memsizes.halfMeg"))

    // should get size as a ConfigMemorySize
    assertEquals(1024 * 1024L, conf.getMemorySize("memsizes.meg").toBytes)
    assertEquals(
      1024 * 1024L,
      conf.getMemorySize("memsizes.megAsNumber").toBytes
    )
    assertEquals(
      Seq(1024 * 1024L, 1024 * 1024L, 1024L * 1024L),
      conf.getMemorySizeList("memsizes.megsList").asScala.map(_.toBytes)
    )
    assertEquals(512 * 1024L, conf.getMemorySize("memsizes.halfMeg").toBytes)
  }

  @Test
  def test01MergingOtherFormats(): Unit = {
    val conf = ConfigFactory.load("test01")

    // should have loaded stuff from .json
    assertEquals(1, conf.getInt("fromJson1"))
    assertEquals("A", conf.getString("fromJsonA"))

    // should have loaded stuff from .properties
    assertEquals("abc", conf.getString("fromProps.abc"))
    assertEquals(1, conf.getInt("fromProps.one"))
    assertEquals(true, conf.getBoolean("fromProps.bool"))
  }

  @Test
  def test01ToString(): Unit = {
    val conf = ConfigFactory.load("test01")

    // toString() on conf objects doesn't throw (toString is just a debug string so not testing its result)
    conf.toString()
  }

  @Test
  def test01SystemFallbacks(): Unit = {
    val conf = ConfigFactory.load("test01")
    val jv = System.getProperty("java.version")
    assertNotNull(jv)
    assertEquals(jv, conf.getString("system.javaversion"))
    val home = System.getenv("HOME")
    if (home != null) {
      assertEquals(home, conf.getString("system.home"))
    } else {
      assertEquals(null, conf.getObject("system").get("home"))
    }
  }

  @Test
  def test01Origins(): Unit = {
    val conf = ConfigFactory.load("test01")
    val path = sys.env
      .getOrElse("testClassesPath", "testClassesPath must be set in build")

    val o1 = conf.getValue("ints.fortyTwo").origin
    // the checkout directory would be in between this startsWith and endsWith
    assertTrue(
      "description starts with resource '" + o1.description + "'",
      o1.description.startsWith("test01.conf @")
    )
    assertTrue(
      "description ends with url and line '" + o1.description + "'",
      o1.description.endsWith(s"$path/test01.conf: 3")
    )
    assertEquals("test01.conf", o1.resource)
    assertTrue(
      "url ends with resource file",
      o1.url.getPath.endsWith(s"$path/test01.conf")
    )
    assertEquals(3, o1.lineNumber)

    val o2 = conf.getValue("fromJson1").origin
    // the checkout directory would be in between this startsWith and endsWith
    assertTrue(
      "description starts with json resource '" + o2.description + "'",
      o2.description.startsWith("test01.json @")
    )
    assertTrue(
      "description of json resource ends with url and line '" + o2.description + "'",
      o2.description.endsWith(s"$path/test01.json: 2")
    )
    assertEquals("test01.json", o2.resource)
    assertTrue(
      "url ends with json resource file",
      o2.url.getPath.endsWith(s"$path/test01.json")
    )
    assertEquals(2, o2.lineNumber)

    val o3 = conf.getValue("fromProps.bool").origin
    // the checkout directory would be in between this startsWith and endsWith
    assertTrue(
      "description starts with props resource '" + o3.description + "'",
      o3.description.startsWith("test01.properties @")
    )
    assertTrue(
      "description of props resource ends with url '" + o3.description + "'",
      o3.description.endsWith(s"$path/test01.properties")
    )
    assertEquals("test01.properties", o3.resource)
    assertTrue(
      "url ends with props resource file",
      o3.url.getPath.endsWith(s"$path/test01.properties")
    )
    // we don't have line numbers for properties files
    assertEquals(-1, o3.lineNumber)
  }

  @Test
  def test01EntrySet(): Unit = {
    val conf = ConfigFactory.load("test01")

    val javaEntries = conf.entrySet
    val entries = Map(
      (javaEntries.asScala.toSeq map { e => (e.getKey(), e.getValue()) }): _*
    )
    assertEquals(Some(intValue(42)), entries.get("ints.fortyTwo"))
    assertEquals(None, entries.get("nulls.null"))
  }

  @Test
  def test01Serializable(): Unit = {
    // we can't ever test an expected serialization here because it
    // will have system props in it that vary by test system,
    // and the ConfigOrigin in there will also vary by test system
    val conf = ConfigFactory.load("test01")
    val confCopy = checkSerializable(conf)
  }

  @Test
  def test02SubstitutionsWithWeirdPaths(): Unit = {
    val conf = ConfigFactory.load("test02")

    assertEquals(42, conf.getInt("42_a"))
    assertEquals(42, conf.getInt("42_b"))
    assertEquals(57, conf.getInt("57_a"))
    assertEquals(57, conf.getInt("57_b"))
    assertEquals(103, conf.getInt("103_a"))
  }

  @Test
  def test02UseWeirdPathsWithConfigObject(): Unit = {
    val conf = ConfigFactory.load("test02")

    // we're checking that the getters in ConfigObject support
    // these weird path expressions
    assertEquals(42, conf.getInt(""" "".""."" """))
    assertEquals(57, conf.getInt("a.b.c"))
    assertEquals(57, conf.getInt(""" "a"."b"."c" """))
    assertEquals(103, conf.getInt(""" "a.b.c" """))
  }

  @Test
  def test03Includes(): Unit = {
    val conf = ConfigFactory.load("test03")

    // include should have overridden the "ints" value in test03
    assertEquals(42, conf.getInt("test01.ints.fortyTwo"))
    // include should have been overridden by 42
    assertEquals(42, conf.getInt("test01.booleans"))
    assertEquals(42, conf.getInt("test01.booleans"))
    // include should have gotten .properties and .json also
    assertEquals("abc", conf.getString("test01.fromProps.abc"))
    assertEquals("A", conf.getString("test01.fromJsonA"))
    // test02 was included
    assertEquals(57, conf.getInt("test02.a.b.c"))
    // equiv01/original.json was included (it has a slash in the name)
    assertEquals("a", conf.getString("equiv01.strings.a"))

    // Now check that substitutions still work
    assertEquals(42, conf.getInt("test01.ints.fortyTwoAgain"))
    assertEquals(
      Seq("a", "b", "c"),
      conf.getStringList("test01.arrays.ofString").asScala
    )
    assertEquals(103, conf.getInt("test02.103_a"))

    // and system fallbacks still work
    val jv = System.getProperty("java.version")
    assertNotNull(jv)
    assertEquals(jv, conf.getString("test01.system.javaversion"))
    val home = System.getenv("HOME")
    if (home != null) {
      assertEquals(home, conf.getString("test01.system.home"))
    } else {
      assertEquals(null, conf.getObject("test01.system").get("home"))
    }
    val concatenated = conf.getString("test01.system.concatenated")
    assertTrue(concatenated.contains("Your Java version"))
    assertTrue(concatenated.contains(jv))
    assertTrue(concatenated.contains(conf.getString("test01.system.userhome")))

    // check that includes into the root object work and that
    // "substitutions look relative-to-included-file first then at root second" works
    assertEquals("This is in the included file", conf.getString("a"))
    assertEquals("This is in the including file", conf.getString("b"))
    assertEquals("This is in the included file", conf.getString("subtree.a"))
    assertEquals("This is in the including file", conf.getString("subtree.b"))
  }

  @Test
  def test04LoadAkkaReference(): Unit = {
    val conf = ConfigFactory.load("test04")

    // Note, test04 is an unmodified old-style akka.conf,
    // which means it has an outer akka{} namespace.
    // that namespace wouldn't normally be used with
    // this library because the conf object is not global,
    // it's per-module already.
    assertEquals("2.0-SNAPSHOT", conf.getString("akka.version"))
    assertEquals(8, conf.getInt("akka.event-handler-dispatcher.max-pool-size"))
    assertEquals(
      "round-robin",
      conf.getString("akka.actor.deployment.\"/app/service-ping\".router")
    )
    assertEquals(true, conf.getBoolean("akka.stm.quick-release"))
  }

  @Test
  def test05LoadPlayApplicationConf(): Unit = {
    val conf = ConfigFactory.load("test05")

    assertEquals("prod", conf.getString("%prod.application.mode"))
    assertEquals("Yet another blog", conf.getString("blog.title"))
  }

  @Test
  def test06Merge(): Unit = {
    // test06 mostly exists because its render() round trip is tricky
    val conf = ConfigFactory.load("test06")

    assertEquals(2, conf.getInt("x"))
    assertEquals(10, conf.getInt("y.foo"))
    assertEquals("world", conf.getString("y.hello"))
  }

  @Test
  def test07IncludingResourcesFromFiles(): Unit = {
    // first, check that when loading from classpath we include another classpath resource
    val fromClasspath =
      ConfigFactory.parseResources(classOf[ConfigTest], "/test07.conf")

    assertEquals(
      "This is to test classpath searches.",
      fromClasspath.getString("test-lib.description")
    )

    // second, check that when loading from a file it falls back to classpath
    val fromFile = ConfigFactory.parseFile(resourceFile("test07.conf"))

    assertEquals(
      "This is to test classpath searches.",
      fromFile.getString("test-lib.description")
    )

    // third, check that a file: URL is the same
    val fromURL =
      ConfigFactory.parseURL(resourceFile("test07.conf").toURI.toURL)

    assertEquals(
      "This is to test classpath searches.",
      fromURL.getString("test-lib.description")
    )
  }

  @Test
  def test08IncludingSlashPrefixedResources(): Unit = {
    // first, check that when loading from classpath we include another classpath resource
    val fromClasspath =
      ConfigFactory.parseResources(classOf[ConfigTest], "/test08.conf")

    assertEquals(
      "This is to test classpath searches.",
      fromClasspath.getString("test-lib.description")
    )

    // second, check that when loading from a file it falls back to classpath
    val fromFile = ConfigFactory.parseFile(resourceFile("test08.conf"))

    assertEquals(
      "This is to test classpath searches.",
      fromFile.getString("test-lib.description")
    )

    // third, check that a file: URL is the same
    val fromURL =
      ConfigFactory.parseURL(resourceFile("test08.conf").toURI.toURL)

    assertEquals(
      "This is to test classpath searches.",
      fromURL.getString("test-lib.description")
    )
  }

  @Test
  def test09DelayedMerge(): Unit = {
    val conf =
      ConfigFactory.parseResources(classOf[ConfigTest], "/test09.conf")
    assertEquals(
      classOf[ConfigDelayedMergeObject].getSimpleName,
      conf.root.get("a").getClass.getSimpleName
    )
    assertEquals(
      classOf[ConfigDelayedMerge].getSimpleName,
      conf.root.get("b").getClass.getSimpleName
    )

    // a.c should work without resolving because no more merging is needed to compute it
    assertEquals(3, conf.getInt("a.c"))

    intercept[ConfigException.NotResolved] {
      conf.getInt("a.q")
    }

    // be sure resolving doesn't throw
    val resolved = conf.resolve()
    assertEquals(3, resolved.getInt("a.c"))
    assertEquals(5, resolved.getInt("b"))
    assertEquals(10, resolved.getInt("a.q"))
  }

  @Test
  def test10DelayedMergeRelativizing(): Unit = {
    val conf =
      ConfigFactory.parseResources(classOf[ConfigTest], "/test10.conf")
    val resolved = conf.resolve()
    assertEquals(3, resolved.getInt("foo.a.c"))
    assertEquals(5, resolved.getInt("foo.b"))
    assertEquals(10, resolved.getInt("foo.a.q"))

    assertEquals(3, resolved.getInt("bar.nested.a.c"))
    assertEquals(5, resolved.getInt("bar.nested.b"))
    assertEquals(10, resolved.getInt("bar.nested.a.q"))
  }

  @Test
  def renderRoundTrip(): Unit = {
    val allBooleans = true :: false :: Nil
    val optionsCombos = {
      for (formatted <- allBooleans;
          originComments <- allBooleans;
          comments <- allBooleans;
          json <- allBooleans)
        yield ConfigRenderOptions.defaults
          .setFormatted(formatted)
          .setOriginComments(originComments)
          .setComments(comments)
          .setJson(json)
    }.toSeq

    for (i <- 1 to 10) {
      val numString = i.toString
      val name = "/test" + { if (numString.size == 1) "0" else "" } + numString
      val conf = ConfigFactory.parseResourcesAnySyntax(
        classOf[ConfigTest],
        name,
        ConfigParseOptions.defaults.setAllowMissing(false)
      )
      for (renderOptions <- optionsCombos) {
        val unresolvedRender = conf.root.render(renderOptions)
        val resolved = conf.resolve()
        val resolvedRender = resolved.root.render(renderOptions)
        val unresolvedParsed = ConfigFactory.parseString(
          unresolvedRender,
          ConfigParseOptions.defaults
        )
        val resolvedParsed =
          ConfigFactory.parseString(resolvedRender, ConfigParseOptions.defaults)
        try {
          assertEquals(
            "unresolved options=" + renderOptions,
            conf.root,
            unresolvedParsed.root
          )
          assertEquals(
            "resolved options=" + renderOptions,
            resolved.root,
            resolvedParsed.root
          )
        } catch {
          case e: Throwable =>
            System.err.println("UNRESOLVED diff:")
            showDiff(conf.root, unresolvedParsed.root)
            System.err.println("RESOLVED diff:")
            showDiff(resolved.root, resolvedParsed.root)
            throw e
        }
        if (renderOptions.getJson && !(renderOptions.getComments || renderOptions.getOriginComments)) {
          // should get valid JSON if we don't have comments and are resolved
          val json =
            try {
              ConfigFactory.parseString(
                resolvedRender,
                ConfigParseOptions.defaults.setSyntax(ConfigSyntax.JSON)
              )
            } catch {
              case e: Exception =>
                System.err.println(
                  "resolvedRender is not valid json: " + resolvedRender
                )
                throw e
            }
        }
        // rendering repeatedly should not make the file different (e.g. shouldn't make it longer)
        // unless the debug comments are in there
        if (!renderOptions.getOriginComments) {
          val renderedAgain = resolvedParsed.root.render(renderOptions)
          // TODO the strings should be THE SAME not just the same length,
          // but there's a bug right now that sometimes object keys seem to
          // be re-ordered. Need to fix.
          assertEquals(
            "render changed, resolved options=" + renderOptions,
            resolvedRender.length,
            renderedAgain.length
          )
        }
      }
    }
  }

  @Test
  def serializeRoundTrip(): Unit = {
    for (i <- 1 to 10) {
      val numString = i.toString
      val name = "/test" + { if (numString.size == 1) "0" else "" } + numString
      val conf = ConfigFactory.parseResourcesAnySyntax(
        classOf[ConfigTest],
        name,
        ConfigParseOptions.defaults.setAllowMissing(false)
      )
      val resolved = conf.resolve()
      checkSerializable(resolved)
    }
  }
}
