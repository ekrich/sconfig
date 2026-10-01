package org.ekrich.config.impl

import org.junit.*
import org.junit.Assert.*

import org.ekrich.config.{
  ConfigFactory,
  ConfigRenderOptions,
  ConfigValueFactory,
  ConfigValueType
}

// Issue #630: a non-finite double renders as a quoted string so the output
// stays valid JSON and survives a round trip
class NonFiniteRenderingTest {
  private val json = ConfigRenderOptions.concise
  private val hocon = ConfigRenderOptions.concise.setJson(false)

  @Test def nonFiniteDoublesRenderAsQuotedStrings(): Unit = {
    assertEquals(
      """{"a":"Infinity"}""",
      ConfigFactory.parseString("a = 1e999").root.render(json)
    )
    assertEquals(
      """{"a":"-Infinity"}""",
      ConfigFactory.parseString("a = -1e999").root.render(json)
    )
    assertEquals(
      "\"NaN\"",
      ConfigValueFactory
        .fromAnyRef(java.lang.Double.valueOf(Double.NaN))
        .render(json)
    )
  }

  @Test def hoconModeMatchesJsonMode(): Unit = {
    assertEquals(
      "a=\"Infinity\"",
      ConfigFactory.parseString("a = 1e999").root.render(hocon)
    )
  }

  @Test def nonFiniteDoublesInListsAreQuoted(): Unit = {
    assertEquals(
      """{"a":["Infinity"]}""",
      ConfigFactory.parseString("a = [1e999]").root.render(json)
    )
  }

  @Test def renderIsAFixedPoint(): Unit = {
    val first = ConfigFactory.parseString("a = 1e999").root.render(json)
    assertEquals(
      first,
      ConfigFactory.parseString(first).root.render(json)
    )
    // in HOCON a re-parsed "Infinity" is an unquoted-safe string, so the
    // spelling settles on a=Infinity from the second render on
    val hoconFirst = ConfigFactory.parseString("a = 1e999").root.render(hocon)
    val hoconSecond =
      ConfigFactory.parseString(hoconFirst).root.render(hocon)
    assertEquals(
      hoconSecond,
      ConfigFactory.parseString(hoconSecond).root.render(hocon)
    )
  }

  @Test def reParsedValueRoundTripsThroughGetDouble(): Unit = {
    val rendered = ConfigFactory.parseString("a = 1e999").root.render(json)
    val reparsed = ConfigFactory.parseString(rendered)
    // "Infinity" parses back as a string, the only JSON type that holds it
    assertEquals(
      ConfigValueType.STRING,
      reparsed.getValue("a").valueType
    )
    assertEquals(
      Double.PositiveInfinity,
      reparsed.getDouble("a"),
      0.0
    )
  }
}
