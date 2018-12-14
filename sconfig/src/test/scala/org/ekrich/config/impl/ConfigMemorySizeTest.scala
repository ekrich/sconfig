/**
 *   Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._
import org.ekrich.config.ConfigMemorySize

class ConfigMemorySizeTest extends TestUtils {

  @Test
  def testEquals() {
    assertTrue(
      "Equal ConfigMemorySize are equal",
      ConfigMemorySize.ofBytes(10).equals(ConfigMemorySize.ofBytes(10)))
    assertTrue(
      "Different ConfigMemorySize are not equal",
      !ConfigMemorySize.ofBytes(10).equals(ConfigMemorySize.ofBytes(11)))
  }

  @Test
  def testToUnits() {
    val kilobyte = ConfigMemorySize.ofBytes(1024)
    assertEquals(1024, kilobyte.toBytes)
  }
}
