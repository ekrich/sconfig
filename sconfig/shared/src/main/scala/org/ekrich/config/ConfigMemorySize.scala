/**
 * Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import java.{math as jm}

/**
 * An immutable class representing an amount of memory. Use static factory
 * methods such as [[ConfigMemorySize#ofBytes]] to create instances.
 *
 * @since 1.3.0
 */
object ConfigMemorySize {

  /**
   * Constructs a ConfigMemorySize representing the given number of bytes.
   *
   * @since 1.3.0
   * @param bytes
   *   a number of bytes
   * @return
   *   an instance representing the number of bytes
   */
  def ofBytes(bytes: jm.BigInteger) = new ConfigMemorySize(bytes)

  /**
   * Constructs a ConfigMemorySize representing the given number of bytes.
   * @param bytes
   *   a number of bytes
   * @return
   *   an instance representing the number of bytes
   */
  def ofBytes(bytes: Long): ConfigMemorySize = new ConfigMemorySize(
    jm.BigInteger.valueOf(bytes)
  )

}

final class ConfigMemorySize private (val bytes: jm.BigInteger) {
  if (bytes.signum() < 0)
    throw new IllegalArgumentException(
      "Attempt to construct ConfigMemorySize with negative number: " + bytes
    )

  /**
   * Gets the size in bytes.
   *
   * @since 1.3.0
   * @return
   *   how many bytes
   * @exception
   *   IllegalArgumentException when memory value in bytes doesn't fit in a long
   *   value. Consider using {@link #toBytesBigInteger} in this case.
   */
  def toBytes: Long =
    if (bytes.bitLength() < 64) bytes.longValue()
    else
      throw new IllegalArgumentException(
        "size-in-bytes value is out of range for a 64-bit long: '" + bytes + "'"
      )

  /**
   * Gets the size in bytes. The behavior of this method is the same as that of
   * the {@link # toBytes ( )} method, except that the number of bytes returned
   * as a BigInteger value. Use it when memory value in bytes doesn't fit in a
   * long value.
   *
   * @return
   *   how many bytes
   */
  def toBytesBigInteger: jm.BigInteger = bytes

  override def toString: String = "ConfigMemorySize(" + bytes + ")"

  override def equals(other: Any): Boolean =
    other match {
      case size: ConfigMemorySize => size.bytes.equals(this.bytes)
      case _                      => false
    }

  override def hashCode: Int =
    // in Java 8 this can become Long.hashCode(bytes)
    // Long.valueOf(bytes).hashCode
    bytes.hashCode()
}
