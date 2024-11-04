package org.ekrich.config.impl

import java.io.File

// See: https://github.com/scala-native/scala-native/issues/4077
object TestPath {
  def file(): File = new File("sconfig/native/src/test/resources")
}
