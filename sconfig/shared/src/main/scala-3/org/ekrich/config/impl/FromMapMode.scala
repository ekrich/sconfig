package org.ekrich.config.impl

import java.{lang => jl}

enum FromMapMode extends jl.Enum[FromMapMode] {
  case KEYS_ARE_PATHS, KEYS_ARE_KEYS
}
