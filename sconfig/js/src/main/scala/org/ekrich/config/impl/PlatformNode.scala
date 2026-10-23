package org.ekrich.config.impl

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

import org.ekrich.config.ConfigException

/**
 * Access to Node.js built-in modules for the Scala.js file shim.
 *
 * The `require` and `process` values are parameters, not read from the global
 * object here, so the browser case (neither is present) is a pure function call
 * that tests can exercise without a browser.
 */
object PlatformNode {
  def fs: js.Dynamic = builtin("fs", g.require, g.process)

  def process: Option[js.Dynamic] = {
    val process = g.process
    if (js.typeOf(process) == "undefined") None
    else Some(process.asInstanceOf[js.Dynamic])
  }

  def builtin(
      name: String,
      requireFn: js.Any,
      processValue: js.Any
  ): js.Dynamic =
    if (js.typeOf(requireFn) == "function")
      requireFn
        .asInstanceOf[js.Function1[String, js.Any]](name)
        .asInstanceOf[js.Dynamic]
    else if (js.typeOf(processValue) != "undefined") {
      val process = processValue.asInstanceOf[js.Dynamic]
      val mainModule = process.mainModule
      if (
        js.typeOf(mainModule) != "undefined" &&
        js.typeOf(mainModule.require) == "function"
      )
        mainModule.require(name).asInstanceOf[js.Dynamic]
      else if (js.typeOf(process.getBuiltinModule) == "function")
        process.getBuiltinModule(name).asInstanceOf[js.Dynamic]
      else unavailable(name)
    } else unavailable(name)

  private def unavailable(name: String): Nothing =
    throw new ConfigException.Generic(
      "The Node.js module '" + name + "' is not available in this JavaScript environment: " +
        "file includes on Scala.js need Node.js. Supply a ConfigIncluder with " +
        "ConfigParseOptions.setIncluder to resolve includes without a file system."
    )
}
