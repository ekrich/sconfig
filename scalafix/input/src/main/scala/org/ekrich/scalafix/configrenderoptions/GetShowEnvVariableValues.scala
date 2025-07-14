/*
rule = ReplaceTypesafeConfig
 */
package org.ekrich.scalafix.configrenderoptions

import com.typesafe.config.ConfigRenderOptions

trait GetShowEnvVariableValues {
  def configRenderOptions: ConfigRenderOptions
  def getShowEnvVariableValues(): Unit
}

object GetShowEnvVariableValues {
  def apply(a: GetShowEnvVariableValues): Unit = {
    a.configRenderOptions.getShowEnvVariableValues()

    a.getShowEnvVariableValues()
  }
}
