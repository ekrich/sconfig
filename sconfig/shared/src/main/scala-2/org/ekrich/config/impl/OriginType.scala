package org.ekrich.config.impl

import java.{lang => jl}

// caution: ordinals used in serialization
final class OriginType private (name: String, ordinal: Int)
    extends jl.Enum[OriginType](name, ordinal)

object OriginType {
  final val GENERIC = new OriginType("GENERIC", 0)
  final val FILE = new OriginType("FILE", 1)
  final val URL = new OriginType("URL", 2)
  final val RESOURCE = new OriginType("RESOURCE", 3)
  final val ENV_VARIABLE = new OriginType("ENV_VARIABLE", 4)

  private[this] final val _values: Array[OriginType] =
    Array(GENERIC, FILE, URL, RESOURCE)

  def values: Array[OriginType] = _values.clone()

  def valueOf(name: String): OriginType =
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException("No enum const OriginType." + name)
    }
}
