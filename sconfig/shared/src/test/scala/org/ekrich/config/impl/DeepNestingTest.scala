package org.ekrich.config.impl

import org.junit.*
import org.junit.Assert.*

import org.ekrich.config.{ConfigException, ConfigFactory}
import org.ekrich.config.parser.ConfigDocumentFactory

// Issue #632: deeply nested input overflows the stack and escapes as an Error;
// the parser rejects it with ConfigException instead
class DeepNestingTest {
  private val deepObjects =
    ("k { " * 105) + "flag = true" + (" }" * 105)
  private val deepArrays = "a = " + ("[" * 105) + "1" + ("]" * 105)

  @Test def nestedObjectsPastTheLimitFailToParse(): Unit = {
    val e = assertThrows(classOf[ConfigException.Parse], () =>
      ConfigFactory.parseString(deepObjects)
    )
    assertTrue(e.getMessage.contains("nesting"))
  }

  @Test def nestedArraysPastTheLimitFailToParse(): Unit = {
    val e = assertThrows(classOf[ConfigException.Parse], () =>
      ConfigFactory.parseString(deepArrays)
    )
    assertTrue(e.getMessage.contains("nesting"))
  }

  @Test def documentParserEnforcesTheSameLimit(): Unit = {
    val e = assertThrows(classOf[ConfigException.Parse], () =>
      ConfigDocumentFactory.parseString(deepObjects)
    )
    assertTrue(e.getMessage.contains("nesting"))
  }

  @Test def nestingJustUnderTheLimitParses(): Unit = {
    val text = ("k { " * 99) + "flag = true" + (" }" * 99)
    val conf = ConfigFactory.parseString(text)
    val path = ((1 to 99).map(_ => "k") :+ "flag").mkString(".")
    assertTrue(conf.getBoolean(path))
  }
}
