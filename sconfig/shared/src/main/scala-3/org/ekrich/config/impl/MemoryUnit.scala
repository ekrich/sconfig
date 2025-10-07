package org.ekrich.config.impl

import java.{lang => jl}
import java.{math => jm}
import java.{util => ju}

enum MemoryUnit(val prefix: String, val powerOf: Int, val power: Int)
    extends jl.Enum[MemoryUnit] {
  val bytes = jm.BigInteger.valueOf(powerOf.toLong).pow(power)

  case BYTES extends MemoryUnit("", 1024, 0)
  case KILOBYTES extends MemoryUnit("kilo", 1000, 1)
  case MEGABYTES extends MemoryUnit("mega", 1000, 2)
  case GIGABYTES extends MemoryUnit("giga", 1000, 3)
  case TERABYTES extends MemoryUnit("tera", 1000, 4)
  case PETABYTES extends MemoryUnit("peta", 1000, 5)
  case EXABYTES extends MemoryUnit("exa", 1000, 6)
  case ZETTABYTES extends MemoryUnit("zetta", 1000, 7)
  case YOTTABYTES extends MemoryUnit("yotta", 1000, 8)

  case KIBIBYTES extends MemoryUnit("kibi", 1024, 1)
  case MEBIBYTES extends MemoryUnit("mebi", 1024, 2)
  case GIBIBYTES extends MemoryUnit("gibi", 1024, 3)
  case TEBIBYTES extends MemoryUnit("tebi", 1024, 4)
  case PEBIBYTES extends MemoryUnit("pebi", 1024, 5)
  case EXBIBYTES extends MemoryUnit("exbi", 1024, 6)
  case ZEBIBYTES extends MemoryUnit("zebi", 1024, 7)
  case OBIBYTES extends MemoryUnit("yobi", 1024, 8)

}

private object MemoryUnit {

  private lazy val unitsMap: ju.Map[String, MemoryUnit] = {
    val map = new ju.HashMap[String, MemoryUnit]
    MemoryUnit.values.foreach { unit =>
      map.put(unit.prefix + "byte", unit)
      map.put(unit.prefix + "bytes", unit)
      if (unit.prefix.length == 0) {
        map.put("b", unit)
        map.put("B", unit)
        map.put("", unit) // no unit specified means bytes
      } else {
        val first = unit.prefix.substring(0, 1)
        val firstUpper = first.toUpperCase
        if (unit.powerOf == 1024) {
          map.put(first, unit) // 512m
          map.put(firstUpper, unit) // 512M
          map.put(firstUpper + "i", unit) // 512Mi
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
