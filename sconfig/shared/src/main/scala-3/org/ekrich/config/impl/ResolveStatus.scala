package org.ekrich.config.impl

import java.{lang => jl}
import java.{util => ju}
import ScalaOps.*

/**
 * Status of substitution resolution.
 */
enum ResolveStatus extends jl.Enum[ResolveStatus] {
  case UNRESOLVED, RESOLVED
}

object ResolveStatus {
  def fromValues(
      values: ju.Collection[_ <: AbstractConfigValue]
  ): ResolveStatus =
    values.scalaOps.findFold(_.resolveStatus == ResolveStatus.UNRESOLVED)(() =>
      ResolveStatus.RESOLVED /* default not found */
    )(_ => ResolveStatus.UNRESOLVED)

  def fromBoolean(resolved: Boolean): ResolveStatus =
    if (resolved) ResolveStatus.RESOLVED else ResolveStatus.UNRESOLVED
}
