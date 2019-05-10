package org.ekrich.config

import java.net.{URI, URL}
import java.util.Properties

object compatOps {

  implicit class uriOps(val uri: URI) extends AnyVal {
    def toURL(): URL = ???
  }

  implicit class threadOps(val thread: Thread) extends AnyVal {
    def getContextClassLoader(): ClassLoader = ???
  }

  implicit class propertiesOps(val prop: Properties) extends AnyVal {
      // can be added to Scala.js
    def load(reader: java.io.Reader): Unit = ???
  }
}