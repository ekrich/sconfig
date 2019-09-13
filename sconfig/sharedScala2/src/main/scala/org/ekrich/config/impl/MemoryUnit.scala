package org.ekrich.config.impl

import java.{lang => jl}
import java.{math => jm}
import java.{util => ju}

private[impl] final class MemoryUnit private[impl] (name: String,
                                                    ordinal: Int,
                                                    val prefix: String,
                                                    val powerOf: Int,
                                                    val power: Int)
    extends jl.Enum[MemoryUnit](name, ordinal) {
  val bytes = jm.BigInteger.valueOf(powerOf.toLong).pow(power)
}

private object MemoryUnit {

  final val BYTES      = new MemoryUnit("BYTES", 0, "", 1024, 0)
  final val KILOBYTES  = new MemoryUnit("KILOBYTES", 1, "kilo", 1000, 1)
  final val MEGABYTES  = new MemoryUnit("MEGABYTES", 2, "mega", 1000, 2)
  final val GIGABYTES  = new MemoryUnit("GIGABYTES", 3, "giga", 1000, 3)
  final val TERABYTES  = new MemoryUnit("TERABYTES", 4, "tera", 1000, 4)
  final val PETABYTES  = new MemoryUnit("PETABYTES", 5, "peta", 1000, 5)
  final val EXABYTES   = new MemoryUnit("EXABYTES", 6, "exa", 1000, 6)
  final val ZETTABYTES = new MemoryUnit("ZETTABYTES", 7, "zetta", 1000, 7)
  final val YOTTABYTES = new MemoryUnit("YOTTABYTES", 8, "yotta", 1000, 8)

  final val KIBIBYTES = new MemoryUnit("KIBIBYTES", 9, "kibi", 1024, 1)
  final val MEBIBYTES = new MemoryUnit("MEBIBYTES", 10, "mebi", 1024, 2)
  final val GIBIBYTES = new MemoryUnit("GIBIBYTES", 11, "gibi", 1024, 3)
  final val TEBIBYTES = new MemoryUnit("TEBIBYTES", 12, "tebi", 1024, 4)
  final val PEBIBYTES = new MemoryUnit("PEBIBYTES", 13, "pebi", 1024, 5)
  final val EXBIBYTES = new MemoryUnit("EXBIBYTES", 14, "exbi", 1024, 6)
  final val ZEBIBYTES = new MemoryUnit("ZEBIBYTES", 15, "zebi", 1024, 7)
  final val OBIBYTES  = new MemoryUnit("OBIBYTES", 16, "yobi", 1024, 8)

  private[this] val _values: Array[MemoryUnit] =
    Array(
      BYTES,
      KILOBYTES,
      MEGABYTES,
      GIGABYTES,
      TERABYTES,
      PETABYTES,
      EXABYTES,
      ZETTABYTES,
      YOTTABYTES,
      KIBIBYTES,
      MEBIBYTES,
      GIBIBYTES,
      TEBIBYTES,
      PEBIBYTES,
      EXBIBYTES,
      ZEBIBYTES,
      OBIBYTES
    )

  def values(): Array[MemoryUnit] = _values.clone()

  def valueOf(name: String): MemoryUnit = {
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException("No enum const MemoryUnit." + name)
    }
  }

  lazy val unitsMap: ju.Map[String, MemoryUnit] = {
    val map = new ju.HashMap[String, MemoryUnit]
    for (unit <- MemoryUnit.values()) {
      map.put(unit.prefix + "byte", unit)
      map.put(unit.prefix + "bytes", unit)
      if (unit.prefix.length == 0) {
        map.put("b", unit)
        map.put("B", unit)
        map.put("", unit) // no unit specified means bytes
      } else {
        val first      = unit.prefix.substring(0, 1)
        val firstUpper = first.toUpperCase
        if (unit.powerOf == 1024) {
          map.put(first, unit)             // 512m
          map.put(firstUpper, unit)        // 512M
          map.put(firstUpper + "i", unit)  // 512Mi
          map.put(firstUpper + "iB", unit) // 512MiB
        } else if (unit.powerOf == 1000) {
          if (unit.power == 1) map.put(first + "B", unit) // 512kB
          else map.put(firstUpper + "B", unit) // 512MB
        } else throw new RuntimeException("broken MemoryUnit enum")
      }
    }
    map
  }
  private[impl] def parseUnit(unit: String): MemoryUnit = unitsMap.get(unit)
}
