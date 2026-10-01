package org.ekrich.config.impl

import org.junit.*
import org.junit.Assert.*

import org.ekrich.config.{ConfigException, ConfigFactory}

// Issue #632: many += lines on one key overflow the stack during resolve and
// escape as an Error; JVM-only because the overflow point depends on the
// thread stack size
class DeepResolveTest {
  private def plusEqualsLines(n: Int): String =
    (1 to n).map(i => s"modules += m$i").mkString("\n")

  @Test def manyPlusEqualsLinesFailToResolveWithConfigException(): Unit = {
    val conf = ConfigFactory.parseString(plusEqualsLines(2000))
    val e = assertThrows(classOf[ConfigException.Parse], () =>
      conf.resolve()
    )
    assertTrue(e.getMessage.contains("stack overflow"))
  }

  @Test def fewPlusEqualsLinesStillResolve(): Unit = {
    val conf = ConfigFactory.parseString(plusEqualsLines(300)).resolve()
    assertEquals(300, conf.getList("modules").size())
  }
}
