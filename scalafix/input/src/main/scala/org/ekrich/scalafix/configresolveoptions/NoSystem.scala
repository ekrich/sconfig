/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configresolveoptions

import com.typesafe.config.ConfigResolveOptions

object NoSystem {
  ConfigResolveOptions.noSystem()
}
