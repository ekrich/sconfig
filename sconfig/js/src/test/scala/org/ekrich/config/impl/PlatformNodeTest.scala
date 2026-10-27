package org.ekrich.config.impl

import scala.scalajs.js

import org.junit.Assert._
import org.junit.Test

import org.ekrich.config.ConfigException

/**
 * The Node.js module lookup is taken as values, so the browser case (no
 * `require`, no `process`) can be tested without a browser: it has to throw a
 * `ConfigException` that points at `ConfigParseOptions.setIncluder`, not an
 * `UnsupportedOperationException` or a JavaScript `TypeError`.
 */
class PlatformNodeTest {
  @Test def noNodeEnvironmentThrowsConfigException(): Unit = {
    try {
      PlatformNode.builtin("fs", js.undefined, js.undefined)
      fail("expected a ConfigException outside Node.js")
    } catch {
      case e: ConfigException =>
        val message = e.getMessage
        assertTrue(
          "the message should name ConfigParseOptions.setIncluder: " + message,
          message != null && message.contains("setIncluder")
        )
    }
  }

  @Test def requireFunctionIsUsed(): Unit = {
    val require =
      js.Any.fromFunction1((name: String) => js.Dynamic.literal(module = name))
    val fs = PlatformNode.builtin("fs", require, js.undefined)
    assertEquals("fs", fs.module.asInstanceOf[String])
  }
}
