package org.ekrich.config.impl

import org.ekrich.config.ConfigException

final class ConfigNodeComment(val comment: Token)
    extends ConfigNodeSingleToken(comment) {

  if (!Tokens.isComment(token))
    throw new ConfigException.BugOrBroken(
      "Tried to create a ConfigNodeComment from a non-comment token")

  private[impl] def commentText: String = Tokens.getCommentText(token)
}
