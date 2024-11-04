/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit._

import org.ekrich.config.ConfigException

class ValidationSerializableTest extends TestUtils {
  @Test
  def validationFailedSerializable(): Unit = {
    // Reusing a previous test case to generate an error
    val reference = parseConfig("""{ a : [{},{},{}] }""")
    val conf = parseConfig("""{ a : 42 }""")
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference)
    }
    val expecteds = Seq(WrongType("a", 1, "list", "number"))

    val actual = checkSerializableNoMeaningfulEquals(e)
    checkValidationException(actual, expecteds)
  }
}
