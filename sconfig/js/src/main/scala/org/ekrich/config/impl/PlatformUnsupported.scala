package org.ekrich.config.impl

import org.ekrich.config.ConfigException

/**
 * The error thrown by every `java.io` and `java.net` stub on Scala.js, where
 * the library does no IO and the caller supplies it through a `ConfigIncluder`.
 *
 * `ConfigException.Generic` and not `ConfigException.IO`: the includer reads an
 * `IO` error as "the next extension may exist" and as "allow missing"
 * (`SimpleIncluder.fromBasename`), and `Parseable` turns an `IOException` into
 * a missing include when `allowMissing` is set, so an `IO` error here would
 * disappear.
 */
object PlatformUnsupported {
  def apply(what: String): ConfigException.Generic =
    new ConfigException.Generic(
      what + " is not available in sconfig on Scala.js: there is no file, URL or " +
        "classpath access. To use include statements, pass your own ConfigIncluder " +
        "with ConfigParseOptions.setIncluder (see docs/SCALA_NATIVE.md, Includes)."
    )
}
