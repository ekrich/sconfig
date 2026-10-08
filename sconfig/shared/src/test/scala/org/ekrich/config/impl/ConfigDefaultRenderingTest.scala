package org.ekrich.config.impl

import org.junit.*
import org.junit.Assert.*

import java.{util => ju}

import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigFormatOptions
import org.ekrich.config.ConfigIncludeContext
import org.ekrich.config.ConfigIncluder
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigSyntax

import scala.jdk.CollectionConverters.*

// Regression tests for rendering old behaviour compatibility
class ConfigDefaultRenderingTest extends RenderingTestSuite {
  private implicit val defaultFormatOptions: ConfigFormatOptions =
    ConfigFormatOptions.defaults

  @Test
  def newLineAtTheEnd(): Unit = {
    val in = """r {
               |}""".stripMargin
    val result = formatHocon(in)
    val expected = """r {}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useFourSpacesIndentation(): Unit = {
    val in = """r {
               |  p {
               |        d {
               |        s: ${r.ss}
               |        }
               |     }
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    p {
                     |        d {
                     |            s = ${r.ss}
                     |        }
                     |    }
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def useEqualsAsAssignSign(): Unit = {
    val in = """r {
               |    s=t_f
               |      "n-m"=1
               |    n:"ALA"
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """r {
                     |    n = ALA
                     |    n-m = 1
                     |    s = t_f
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def dontSimplifyOneEntryNestedObjects(): Unit = {
    val in = """r.p.d= 42"""
    val result = formatHocon(in)

    val expected =
      """r {
        |    p {
        |        d = 42
        |    }
        |}
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def properArrayConcat(): Unit = {
    val in =
      """except: ${ex1} ${ex2}
        |myEmpty: " "
        |""".stripMargin
    val result = formatHocon(in)

    val expected =
      """except = ${ex1} ${ex2}
        |myEmpty = " "
        |""".stripMargin
    checkEqualsAndStable(expected, result)
  }
  // an object nested inside an array is never itself "at root" - it always
  // needs its own braces, first entry included. Porting lightbend/config#832.
  @Test
  def listElementsKeepTheirBraces(): Unit = {
    val in = """root = [{foo = bar}, {baz = qux}]"""
    val result = formatHocon(in)

    val expected = """root = [
                     |    {
                     |        foo = bar
                     |    },
                     |    {
                     |        baz = qux
                     |    }
                     |]
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // the space between two substitutions in a concatenation is unquoted in
  // the source; re-quoting it on render turns a valid array concatenation
  // into a parse error on the way back in. Porting lightbend/config#841.
  @Test
  def arrayConcatenationRoundTripsThroughRender(): Unit = {
    val in = """ex1 = [1, 2]
               |except = ${ex1} ${ex1}""".stripMargin
    val result = formatHocon(in)

    val resolved =
      ConfigFactory.parseString(result, ConfigParseOptions.defaults).resolve()
    assertEquals(List(1, 2, 1, 2), resolved.getIntList("except").asScala)
  }

  // An unresolved merge under a key renders as repeated key/value entries. The
  // banner it used to carry parsed back as comments on those values, so every
  // pass re-emitted them and added one of its own.
  @Test
  def unresolvedMergesRenderToAFixedPoint(): Unit = {
    val inputs = List(
      """a : [1]
        |a += 2""".stripMargin,
      """a : 1
        |a : ${a}""".stripMargin,
      """path = [ /bin ]
        |path = ${path} [ /usr/bin ]""".stripMargin,
      """path : "a:b:c"
        |path : ${path}":d"""".stripMargin,
      """foo : { a : { c : 1 } }
        |foo : ${foo.a}
        |foo : { a : 2 }""".stripMargin,
      """a : 1
        |b : 2
        |a : ${b}
        |b : ${a}""".stripMargin,
      """# one
        |a : 1
        |# two
        |a : ${a}""".stripMargin
    )
    inputs.foreach { in =>
      val result = formatHocon(in)
      checkReparses(result)
      checkEqualObjects(result, formatHocon(result))
    }
  }

  @Test
  def commentsStayWithTheirMergedValue(): Unit = {
    val in = """# one
               |a : 1
               |# two
               |a : ${a}""".stripMargin
    val result = formatHocon(in)

    val expected = """# one
                     |"a" : 1,
                     |# two
                     |"a" : ${a}
                     |
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def nestedUnresolvedMergeIndentsLikeItsSiblings(): Unit = {
    val in = """outer {
               |  sib : 0
               |  a : 1
               |  a : ${outer.a}
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """outer {
                     |    "a" : 1,
                     |    "a" : ${outer.a}
                     |
                     |    sib = 0
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def commentsOnTheMergeItselfSurvive(): Unit = {
    val root = ConfigFactory.parseString("a : 1\na : ${a}", parseOptions).root
    val merge = root.get("a")
    val tagged =
      merge.withOrigin(
        merge.origin.withComments(ju.Collections.singletonList("kept"))
      )
    val result = root
      .withValue("a", tagged)
      .render(
        myDefaultRenderOptions.setConfigFormatOptions(defaultFormatOptions)
      )

    val expected = """# kept
                     |"a" : 1,
                     |"a" : ${a}
                     |
                     |""".stripMargin
    checkEqualObjects(expected, result)
  }

  // with no key to spell it out as repeated entries, a merge can only be
  // described
  @Test
  def unresolvedMergeRenderedWithoutAKeyIsDescribed(): Unit = {
    val merge =
      ConfigFactory.parseString("a : 1\na : ${a}", parseOptions).root.get("a")
    val options =
      myDefaultRenderOptions.setConfigFormatOptions(defaultFormatOptions)

    val expected =
      """# unresolved merge of 2 values follows (
        |# this unresolved merge will not be parseable because it's at the root of the object
        |# the HOCON format has no way to list multiple root objects in a single file
        |#     unmerged value 0 from String: 1
        |1,
        |#     unmerged value 1 from String: 2
        |${a}
        |# ) end of unresolved merge
        |""".stripMargin
    checkEqualObjects(expected, merge.render(options))
    checkEqualObjects("1,\n${a}\n", merge.render(options.setComments(false)))
  }

  @Test
  def unresolvedMergeInsideArrayElementRendersToAFixedPoint(): Unit = {
    val in = """l = [ { a : 1
               |a : ${x} } ]""".stripMargin
    val result = formatHocon(in)

    val expected = """l = [
                     |    {
                     |        "a" : 1,
                     |        "a" : ${x}
                     |
                     |    }
                     |]
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  @Test
  def commentsOfNestedUnresolvedMergeIndentLikeItsSiblings(): Unit = {
    val in = """outer {
               |  sib : 0
               |  # c1
               |  a : 1
               |  # c2
               |  a : ${outer.a}
               |}""".stripMargin
    val result = formatHocon(in)

    val expected = """outer {
                     |    # c1
                     |    "a" : 1,
                     |    # c2
                     |    "a" : ${outer.a}
                     |
                     |    sib = 0
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // sconfig-only: ConfigParseOptions.setKeepCommentsAcrossBlankLines. Off, the
  // parser drops a comment block that a blank line separates from the field
  // below it, as lightbend/config does; on, the block stays with that field, in
  // the origin and in the render. Documented in docs/NEW_FEATURES.md, wired in
  // ConfigParser.parseObject, parseArray and parse.
  private val keepCommentsAcrossBlankLines =
    parseOptions.setKeepCommentsAcrossBlankLines(true)

  @Test
  def keepCommentsAcrossBlankLinesIsOffByDefault(): Unit = {
    assertFalse(ConfigParseOptions.defaults.getKeepCommentsAcrossBlankLines)
    assertFalse(parseOptions.getKeepCommentsAcrossBlankLines)
    assertTrue(keepCommentsAcrossBlankLines.getKeepCommentsAcrossBlankLines)
  }

  // an includer that parses its content with the options the include context
  // hands it, the way SimpleIncluder parses a real include
  private def parsingIncluder(content: String): ConfigIncluder =
    new ConfigIncluder {
      def include(
          context: ConfigIncludeContext,
          what: String
      ): ConfigObject =
        ConfigFactory.parseString(content, context.parseOptions).root
      def withFallback(fallback: ConfigIncluder): ConfigIncluder = this
    }

  // sconfig-only API, like the class's other setters: the same value returns
  // this, and the flag toggles both ways
  @Test
  def theOptionSetterIsIdempotentAndTogglesBack(): Unit = {
    assertSame(
      parseOptions,
      parseOptions.setKeepCommentsAcrossBlankLines(false)
    )
    assertSame(
      keepCommentsAcrossBlankLines,
      keepCommentsAcrossBlankLines.setKeepCommentsAcrossBlankLines(true)
    )

    val off =
      keepCommentsAcrossBlankLines.setKeepCommentsAcrossBlankLines(false)
    assertFalse(off.getKeepCommentsAcrossBlankLines)
    checkEqualsAndStable("a = 1\n", formatHoconWith("# header\n\na = 1\n", off))
  }

  // sconfig-only API: toggling the flag on preserves the configured syntax,
  // origin description, allowMissing, includer and class loader, and leaves the
  // configured instance alone. The class keeps identity equality, so a copy
  // with the same fields is a new instance, not an equal one.
  @Test
  def theOptionSetterPreservesTheOtherFields(): Unit = {
    val includer = parsingIncluder("x = 2\n")
    val classLoader = new TestClassLoader(null, Map.empty)
    val configured = ConfigParseOptions.defaults
      .setSyntax(ConfigSyntax.CONF)
      .setOriginDescription("custom")
      .setAllowMissing(false)
      .setIncluder(includer)
      .setClassLoader(classLoader)

    val toggled = configured.setKeepCommentsAcrossBlankLines(true)
    assertSame(ConfigSyntax.CONF, toggled.getSyntax)
    checkEqualObjects("custom", toggled.getOriginDescription)
    assertFalse(toggled.getAllowMissing)
    assertSame(includer, toggled.getIncluder)
    assertSame(classLoader, toggled.getClassLoader)

    assertFalse(configured.getKeepCommentsAcrossBlankLines)
    val roundTripped = toggled.setKeepCommentsAcrossBlankLines(false)
    assertNotSame(configured, roundTripped)
    assertNotEquals(configured, roundTripped)
  }

  // sconfig-only API: every copy path carries the flag along with the field it
  // changes, so a configured instance keeps the option
  @Test
  def everyCopyPathPreservesTheOption(): Unit = {
    val includer = parsingIncluder("x = 2\n")
    val classLoader = new TestClassLoader(null, Map.empty)
    val copies = List(
      keepCommentsAcrossBlankLines.setSyntax(ConfigSyntax.CONF),
      keepCommentsAcrossBlankLines.setSyntaxFromFilename("x.conf"),
      keepCommentsAcrossBlankLines.setOriginDescription("custom"),
      keepCommentsAcrossBlankLines.withFallbackOriginDescription("fallback"),
      keepCommentsAcrossBlankLines.setAllowMissing(false),
      keepCommentsAcrossBlankLines.setIncluder(includer),
      keepCommentsAcrossBlankLines.appendIncluder(includer),
      keepCommentsAcrossBlankLines.prependIncluder(includer),
      keepCommentsAcrossBlankLines.setClassLoader(classLoader)
    )
    copies.foreach(options =>
      assertTrue(options.getKeepCommentsAcrossBlankLines)
    )
    assertFalse(
      ConfigParseOptions.defaults
        .setSyntax(ConfigSyntax.CONF)
        .getKeepCommentsAcrossBlankLines
    )
  }

  // sconfig-only: the include context derives its parse options from the
  // caller's, so the option reaches the parse inside the included file too
  @Test
  def theOptionReachesParsingInsideAnIncludedFile(): Unit = {
    val in = "include \"whatever\"\ny = 1\n"
    val on = parseOptions
      .setIncluder(parsingIncluder("# included\n\nx = 2\n"))
      .setKeepCommentsAcrossBlankLines(true)
    val off = parseOptions.setIncluder(parsingIncluder("# included\n\nx = 2\n"))

    checkEqualObjects(
      List(" included"),
      ConfigFactory
        .parseString(in, on)
        .getValue("x")
        .origin
        .comments
        .asScala
        .toList
    )
    checkEqualObjects(
      List[String](),
      ConfigFactory
        .parseString(in, off)
        .getValue("x")
        .origin
        .comments
        .asScala
        .toList
    )
  }

  // sconfig-only: with the option on, a kept block before include attaches to
  // the next local field, not to the included file
  @Test
  def aCommentBlockBeforeIncludeAttachesToTheNextLocalField(): Unit = {
    val in = "# before include\n\ninclude \"whatever\"\ny = 1\n"
    val on = parseOptions
      .setIncluder(parsingIncluder("x = 2\n"))
      .setKeepCommentsAcrossBlankLines(true)
    val off = parseOptions.setIncluder(parsingIncluder("x = 2\n"))

    val parsedOn = ConfigFactory.parseString(in, on)
    checkEqualObjects(
      List[String](),
      parsedOn.getValue("x").origin.comments.asScala.toList
    )
    checkEqualObjects(
      List(" before include"),
      parsedOn.getValue("y").origin.comments.asScala.toList
    )
    checkEqualObjects(
      List[String](),
      ConfigFactory
        .parseString(in, off)
        .getValue("y")
        .origin
        .comments
        .asScala
        .toList
    )
  }

  // not from lightbend/config: it drops the header, and so does the default
  // here, so the option has to be on for the two lines to survive
  @Test
  def commentsSeparatedFromTheFollowingFieldByABlankLineAreKept(): Unit = {
    val in = """# Copyright 2025 Example
               |# Licensed under Apache-2.0
               |
               |a = 1
               |""".stripMargin
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected = """# Copyright 2025 Example
                     |# Licensed under Apache-2.0
                     |a = 1
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // not from lightbend/config: with the option on, the part above the blank
  // line is kept too, not only the part below it
  @Test
  def aBlankLineInsideACommentBlockKeepsTheLinesAboveIt(): Unit = {
    val in = """# part one
               |
               |# part two
               |a = 1
               |""".stripMargin
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected = """# part one
                     |# part two
                     |a = 1
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // not from lightbend/config, like the tests above, for a field nested in an
  // object
  @Test
  def aBlankLineBeforeAFieldInAnObjectKeepsItsComments(): Unit = {
    val in = """r {
               |    # about p
               |
               |    p = 1
               |}
               |""".stripMargin
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected = """r {
                     |    # about p
                     |    p = 1
                     |}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // not from lightbend/config: a whitespace-only line separates blocks like an
  // empty one, so the option keeps the comment there too
  @Test
  def aWhitespaceOnlyLineSeparatesLikeABlankLine(): Unit = {
    val in = "# header\n   \na = 1\n" // the middle line is spaces
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    checkEqualsAndStable("# header\na = 1\n", result)
  }

  // not from lightbend/config: both comment syntaxes are kept, in source order,
  // and the render normalizes them to '#'
  @Test
  def commentsOfBothSyntaxesKeepTheirSourceOrder(): Unit = {
    val in = """# one
               |
               |// two
               |
               |# three
               |a = 1
               |""".stripMargin
    val parsed = ConfigFactory.parseString(in, keepCommentsAcrossBlankLines)
    checkEqualObjects(
      List(" one", " two", " three"),
      parsed.getValue("a").origin.comments.asScala.toList
    )
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected = """# one
                     |# two
                     |# three
                     |a = 1
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // not from lightbend/config: a block that a blank line separates from the
  // array element below it stays on that element; the trailing block has no
  // element after it and is dropped, as on both revisions
  @Test
  def aBlankLineBeforeAnArrayElementKeepsItsComments(): Unit = {
    val in = """a = [
               |# one
               |
               |1,
               |// two
               |
               |2
               |# tail
               |
               |]
               |""".stripMargin
    val parsed = ConfigFactory.parseString(in, keepCommentsAcrossBlankLines)
    val list = parsed.getList("a")
    checkEqualObjects(List(" one"), list.get(0).origin.comments.asScala.toList)
    checkEqualObjects(List(" two"), list.get(1).origin.comments.asScala.toList)
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected = """a = [
                     |    # one
                     |    1,
                     |    # two
                     |    2
                     |]
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }

  // a block with no following field or element has nothing to attach to, so it
  // is dropped as on both revisions (lightbend/config does the same)
  @Test
  def commentsWithNoFollowingFieldAreStillDropped(): Unit = {
    val inObject = """r {
                     |    a = 1
                     |    # trailing
                     |}
                     |""".stripMargin
    checkEqualsAndStable(
      "r {\n    a = 1\n}\n",
      formatHoconWith(inObject, keepCommentsAcrossBlankLines)
    )

    val inFile = """a = 1
                   |# trailing
                   |""".stripMargin
    checkEqualsAndStable(
      "a = 1\n",
      formatHoconWith(inFile, keepCommentsAcrossBlankLines)
    )
  }

  // not from lightbend/config: a header outside a braced root is kept in the
  // root object's origin but is not rendered, since the renderer prints the
  // comments of fields, not of the root object itself
  @Test
  def aHeaderBeforeABracedRootStaysInTheOriginButIsNotRendered(): Unit = {
    val in = """# header
               |
               |{a=1}
               |""".stripMargin
    val parsed = ConfigFactory.parseString(in, keepCommentsAcrossBlankLines)
    checkEqualObjects(
      List(" header"),
      parsed.root.origin.comments.asScala.toList
    )

    checkEqualsAndStable(
      "a = 1\n",
      formatHoconWith(in, keepCommentsAcrossBlankLines)
    )
  }

  // default off: the same inputs drop the comments exactly as lightbend/config
  // does, so origin.comments and the render do not change
  @Test
  def withoutTheOptionBlankLineSeparatedCommentsStayDropped(): Unit = {
    val licence = """# Copyright 2025 Example
                    |# Licensed under Apache-2.0
                    |
                    |a = 1
                    |""".stripMargin
    checkEqualsAndStable("a = 1\n", formatHocon(licence))

    val order = """# one
                  |
                  |// two
                  |
                  |# three
                  |a = 1
                  |""".stripMargin
    checkEqualsAndStable("# three\na = 1\n", formatHocon(order))

    val array = """a = [
                  |# one
                  |
                  |1,
                  |// two
                  |
                  |2
                  |]
                  |""".stripMargin
    checkEqualsAndStable("a = [\n    1,\n    2\n]\n", formatHocon(array))

    val braced = "# header\n\n{a=1}\n"
    checkEqualObjects(
      List[String](),
      ConfigFactory.parseString(braced).root.origin.comments.asScala.toList
    )
  }

  // setSimplifyNestedObjects(true) compresses r.p.x only while the objects
  // carry no comments: with the option on, the kept comment blocks the
  // compression, even when comments are not rendered
  @Test
  def simplifyNestedObjectsDoesNotCompressAnObjectWithKeptComments(): Unit = {
    val simplify =
      ConfigFormatOptions.defaults.setSimplifyNestedObjects(true)
    val in = """r{
               |# obj
               |
               |p{x=1}}""".stripMargin

    checkEqualsAndStable(
      "r {\n    # obj\n    p {\n        x = 1\n    }\n}\n",
      formatHoconWith(in, keepCommentsAcrossBlankLines)(simplify)
    )

    val withoutComments =
      ConfigFactory
        .parseString(in, keepCommentsAcrossBlankLines)
        .root
        .render(
          myDefaultRenderOptions
            .setComments(false)
            .setConfigFormatOptions(simplify)
        )
    checkEqualObjects(
      "r {\n    p {\n        x = 1\n    }\n}\n",
      withoutComments
    )

    // with the option off there are no comments and the path still compresses
    checkEqualObjects("r.p.x = 1\n", formatHocon(in)(simplify))
  }

  // known limitation, pinned so a change is noticed: with the option on, the
  // comment above a += field is printed above the delayed assignment and above
  // its element. The no-gap input duplicates on both revisions, so this is an
  // existing += rendering defect that preservation exposes, not a new parser
  // bug; fixing it belongs in a separate change.
  @Test
  def aPlusEqualsCommentIsPrintedTwiceWithTheOption(): Unit = {
    val in = "a=[]\n# plus\n\na+=2"
    val result = formatHoconWith(in, keepCommentsAcrossBlankLines)

    val expected =
      "\"a\" : [],\n# plus\n\"a\" : ${?a}[\n    # plus\n    2\n]\n\n"
    checkEqualsAndStable(expected, result)
  }

  // the [] list-expansion suffix (lightbend/config#833) is part of the
  // substitution syntax, not resolved by rendering, so it stays verbatim
  @Test
  def envVarListExpansionSubstitutionRendersVerbatim(): Unit = {
    val in = """a = ${FOO[]}
               |b = ${?FOO[]}""".stripMargin
    val result = formatHocon(in)
    val expected = """a = ${FOO[]}
                     |b = ${?FOO[]}
                     |""".stripMargin
    checkEqualsAndStable(expected, result)
  }
}
