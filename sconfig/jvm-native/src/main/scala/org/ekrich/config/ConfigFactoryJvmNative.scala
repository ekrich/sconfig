package org.ekrich.config

/**
 * [[ConfigFactory]] methods common to JVM and Native
 */
abstract class ConfigFactoryJvmNative extends ConfigFactoryShared {
  // parseFile and parseFileAnySyntax should be here but they
  // use shared so then it is very tangled so any refactor is big
  // TODO: first create a PublicApiFileTest for JVM native to test
  // this API on Native.

}
