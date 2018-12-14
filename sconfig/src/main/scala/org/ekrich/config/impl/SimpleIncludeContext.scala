/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.ekrich.config.ConfigIncludeContext
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigParseable

class SimpleIncludeContext(parseable: Parseable, options: ConfigParseOptions)
    extends ConfigIncludeContext {

  def this(parseable: Parseable) =
    this(parseable, SimpleIncluder.clearForInclude(parseable.options))

  private[impl] def withParseable(parseable: Parseable) =
    if (parseable eq this.parseable) this
    else new SimpleIncludeContext(parseable)

  override def relativeTo(filename: String): ConfigParseable = {
    if (ConfigImpl.traceLoadsEnabled)
      ConfigImpl.trace(
        "Looking for '" + filename + "' relative to " + parseable)
    if (parseable != null) parseable.relativeTo(filename) else null
  }

  override def parseOptions: ConfigParseOptions = options

  override def setParseOptions(options: ConfigParseOptions) =
    new SimpleIncludeContext(parseable,
                             options.setSyntax(null).setOriginDescription(null))
}
