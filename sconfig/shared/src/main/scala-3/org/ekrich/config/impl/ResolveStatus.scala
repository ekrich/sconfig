package org.ekrich.config.impl

import java.{lang => jl}
import java.{util => ju}

/**
 * Status of substitution resolution.
 */
enum ResolveStatus extends jl.Enum[ResolveStatus] {
  case UNRESOLVED, RESOLVED
}

object ResolveStatus {
  def fromValues(
      values: ju.Collection[_ <: AbstractConfigValue]
  ): ResolveStatus = {
    import scala.jdk.CollectionConverters._
    values.asScala.find(_.resolveStatus == ResolveStatus.UNRESOLVED) match {
      case Some(_) => ResolveStatus.UNRESOLVED
      case None    => ResolveStatus.RESOLVED
    }
  }

  def fromBoolean(resolved: Boolean): ResolveStatus =
    if (resolved) ResolveStatus.RESOLVED else ResolveStatus.UNRESOLVED
}
