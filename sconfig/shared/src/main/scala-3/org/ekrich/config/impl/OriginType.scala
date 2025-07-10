package org.ekrich.config.impl

import java.{lang => jl}

enum OriginType extends jl.Enum[OriginType] {
  case GENERIC, FILE, URL, RESOURCE, ENV_VARIABLE
}
