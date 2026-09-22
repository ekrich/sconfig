package org.ekrich.config.impl

final class SubstitutionExpression(
    val path: Path,
    val optional: Boolean,
    val listExpansion: Boolean = false
) {
  private[impl] def changePath(newPath: Path) =
    if (newPath eq path) this
    else new SubstitutionExpression(newPath, optional, listExpansion)

  override def toString: String =
    "${" + (if (optional) "?" else "") + path.render + (if (listExpansion)
                                                          "[]"
                                                        else "") + "}"

  override def equals(other: Any): Boolean =
    if (other.isInstanceOf[SubstitutionExpression]) {
      val otherExp =
        other.asInstanceOf[SubstitutionExpression]
      otherExp.path == this.path && otherExp.optional == this.optional &&
        otherExp.listExpansion == this.listExpansion
    } else false

  override def hashCode: Int = {
    var h = 41 * (41 + path.hashCode)
    h = 41 * (h + (if (optional) 1 else 0))
    h = 41 * (h + (if (listExpansion) 1 else 0))
    h
  }
}
