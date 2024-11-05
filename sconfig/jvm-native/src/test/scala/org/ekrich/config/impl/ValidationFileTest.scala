/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit._

import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigException
import FileUtils._

class ValidationFileTest extends TestUtils {
  // TODO: There is a problem upstream where an exception is not thrown
  // when the file does not exist. Added a check in FileUtils for native only
  // jvm would need it.
  @Test
  def validation(): Unit = {
    val reference = ConfigFactory.parseFile(
      resourceFile("validate-reference.conf"),
      ConfigParseOptions.defaults
    )
    val conf = ConfigFactory.parseFile(
      resourceFile("validate-invalid.conf"),
      ConfigParseOptions.defaults
    )
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference)
    }

    val expecteds = Seq(
      Missing("willBeMissing", 1, "number"),
      WrongType("int3", 7, "number", "object"),
      WrongType("float2", 9, "number", "boolean"),
      WrongType("float3", 10, "number", "list"),
      WrongType("bool1", 11, "boolean", "number"),
      WrongType("bool3", 13, "boolean", "object"),
      Missing("object1.a", 17, "string"),
      WrongType("object2", 18, "object", "list"),
      WrongType("object3", 19, "object", "number"),
      WrongElementType("array3", 22, "boolean", "object"),
      WrongElementType("array4", 23, "object", "number"),
      WrongType("array5", 24, "list", "number"),
      WrongType("a.b.c.d.e.f.g", 28, "boolean", "number"),
      Missing("a.b.c.d.e.f.j", 28, "boolean"),
      WrongType("a.b.c.d.e.f.i", 30, "boolean", "list")
    )

    checkValidationException(e, expecteds)
  }

  @Test
  def validationWithRoot(): Unit = {
    val objectWithB = parseObject("""{ b : c }""")
    val reference = ConfigFactory
      .parseFile(
        resourceFile("validate-reference.conf"),
        ConfigParseOptions.defaults
      )
      .withFallback(objectWithB)
    val conf = ConfigFactory.parseFile(
      resourceFile("validate-invalid.conf"),
      ConfigParseOptions.defaults
    )
    val e = intercept[ConfigException.ValidationFailed] {
      conf.checkValid(reference, "a", "b")
    }

    val expecteds = Seq(
      Missing("b", 1, "string"),
      WrongType("a.b.c.d.e.f.g", 28, "boolean", "number"),
      Missing("a.b.c.d.e.f.j", 28, "boolean"),
      WrongType("a.b.c.d.e.f.i", 30, "boolean", "list")
    )

    checkValidationException(e, expecteds)
  }
}
