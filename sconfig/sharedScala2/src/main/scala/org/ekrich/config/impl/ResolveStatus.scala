package org.ekrich.config.impl

import java.{lang => jl}
import java.{util => ju}

/**
 * Status of substitution resolution.
 */
class ResolveStatus private (name: String, ordinal: Int)
    extends jl.Enum[ResolveStatus](name, ordinal)

object ResolveStatus {

  final val UNRESOLVED = new ResolveStatus("UNRESOLVED", 0)
  final val RESOLVED   = new ResolveStatus("RESOLVED", 1)

  private[this] final val _values: Array[ResolveStatus] =
    Array(UNRESOLVED, RESOLVED)

  def values: Array[ResolveStatus] = _values.clone()

  def valueOf(name: String): ResolveStatus =
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException("No enum const ResolveStatus." + name)
    }

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
