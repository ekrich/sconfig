package org.ekrich.config.impl

import java.{lang => jl}

enum ConfigIncludeKind extends jl.Enum[ConfigIncludeKind] {
  case URL, FILE, CLASSPATH, HEURISTIC
}
