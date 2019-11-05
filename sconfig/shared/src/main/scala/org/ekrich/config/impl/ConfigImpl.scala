/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.File
import java.{lang => jl}
import jl.ref.WeakReference
import java.net.URL
import java.time.Duration
import java.{util => ju}
import java.util.concurrent.Callable
import scala.jdk.CollectionConverters._
import org.ekrich.config.Config
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigIncluder
import org.ekrich.config.ConfigMemorySize
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigOrigin
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigParseable
import org.ekrich.config.ConfigValue

/**
 * Internal implementation detail, not ABI stable, do not touch.
 * For use only by the {@link org.ekrich.config} package.
 */
object ConfigImpl {
  private[impl] class LoaderCache private[impl] () {
    private[impl] var currentSystemProperties: Config = null
    private var currentLoader                         = new WeakReference[ClassLoader](null)
    private val cache                                 = new ju.HashMap[String, Config]

    // for now, caching as long as the loader remains the same,
    // drop entire cache if it changes.
    private[impl] def getOrElseUpdate(
        loader: ClassLoader,
        key: String,
        updater: Callable[Config]
    ): Config =
      this.synchronized {
        if (loader != currentLoader.get) {
          // reset the cache if we start using a different loader
          cache.clear()
          currentLoader = new WeakReference[ClassLoader](loader)
        }
        val systemProperties = systemPropertiesAsConfig
        if (systemProperties != currentSystemProperties) {
          cache.clear()
          currentSystemProperties = systemProperties
        }
        var config = cache.get(key)
        if (config == null) {
          // Changed for https://github.com/lampepfl/dotty/issues/7356
          // was config = updater.call()
          config = try {
            updater.call()
          } catch {
            case e: RuntimeException =>
              throw e // this will include ConfigException

            case e: Exception =>
              throw new ConfigException.Generic(e.getMessage, e)
          }
          if (config != null) {
            cache.put(key, config)
          } else {
            throw new ConfigException.BugOrBroken(
              "null config from cache updater"
            )
          }
        }
        config
      }
  }

  private object LoaderCacheHolder {
    private[impl] val cache = new ConfigImpl.LoaderCache()

    private[impl] def invalidate(): Unit = cache.synchronized {
      cache.currentSystemProperties = null
    }
  }

  def computeCachedConfig(
      loader: ClassLoader,
      key: String,
      updater: Callable[Config]
  ): Config = {
    var cache: LoaderCache = null
    try {
      cache = LoaderCacheHolder.cache
    } catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }
    cache.getOrElseUpdate(loader, key, updater)
  }

  private[impl] class FileNameSource extends SimpleIncluder.NameSource {
    override def nameToParseable(
        name: String,
        parseOptions: ConfigParseOptions
    ): ConfigParseable =
      Parseable.newFile(new File(name), parseOptions)
  }

  private[impl] class ClasspathNameSource extends SimpleIncluder.NameSource {
    override def nameToParseable(
        name: String,
        parseOptions: ConfigParseOptions
    ): ConfigParseable =
      Parseable.newResources(name, parseOptions)
  }

  private[impl] class ClasspathNameSourceWithClass(val klass: Class[_])
      extends SimpleIncluder.NameSource {
    override def nameToParseable(
        name: String,
        parseOptions: ConfigParseOptions
    ): ConfigParseable =
      Parseable.newResources(klass, name, parseOptions)
  }

  def parseResourcesAnySyntax(
      klass: Class[_],
      resourceBasename: String,
      baseOptions: ConfigParseOptions
  ): ConfigObject = {
    val source =
      new ConfigImpl.ClasspathNameSourceWithClass(klass)
    SimpleIncluder.fromBasename(source, resourceBasename, baseOptions)
  }

  def parseResourcesAnySyntax(
      resourceBasename: String,
      baseOptions: ConfigParseOptions
  ): ConfigObject = {
    val source = new ConfigImpl.ClasspathNameSource
    SimpleIncluder.fromBasename(source, resourceBasename, baseOptions)
  }

  def parseFileAnySyntax(
      basename: File,
      baseOptions: ConfigParseOptions
  ): ConfigObject = {
    val source = new ConfigImpl.FileNameSource
    SimpleIncluder.fromBasename(source, basename.getPath, baseOptions)
  }

  private[impl] def emptyObject(
      originDescription: String
  ): AbstractConfigObject = {
    val origin =
      if (originDescription != null)
        SimpleConfigOrigin.newSimple(originDescription)
      else null
    emptyObject(origin)
  }

  def emptyConfig(originDescription: String): Config =
    emptyObject(originDescription).toConfig

  private[impl] def empty(origin: ConfigOrigin): AbstractConfigObject =
    emptyObject(origin)

  // default origin for values created with fromAnyRef and no origin specified
  private val defaultValueOrigin =
    SimpleConfigOrigin.newSimple("hardcoded value")
  private val defaultTrueValue =
    new ConfigBoolean(defaultValueOrigin, true)
  private val defaultFalseValue =
    new ConfigBoolean(defaultValueOrigin, false)
  private val defaultNullValue = new ConfigNull(defaultValueOrigin)
  private val defaultEmptyList = new SimpleConfigList(
    defaultValueOrigin,
    ju.Collections.emptyList[AbstractConfigValue]
  )
  private val defaultEmptyObject =
    SimpleConfigObject.empty(defaultValueOrigin)

  private def emptyList(origin: ConfigOrigin): SimpleConfigList =
    if (origin == null || (origin == defaultValueOrigin)) defaultEmptyList
    else
      new SimpleConfigList(
        origin,
        ju.Collections.emptyList[AbstractConfigValue]
      )

  private def emptyObject(origin: ConfigOrigin): AbstractConfigObject = {
    // we want null origin to go to SimpleConfigObject.empty() to get the
    // origin "empty config" rather than "hardcoded value"
    if (origin == defaultValueOrigin) defaultEmptyObject
    else SimpleConfigObject.empty(origin)
  }

  private def valueOrigin(originDescription: String): ConfigOrigin =
    if (originDescription == null) defaultValueOrigin
    else SimpleConfigOrigin.newSimple(originDescription)

  def fromAnyRef(obj: AnyRef, originDescription: String): ConfigValue = {
    val origin = valueOrigin(originDescription)
    fromAnyRef(obj, origin, FromMapMode.KEYS_ARE_KEYS)
  }

  def fromPathMap(
      pathMap: ju.Map[String, _],
      originDescription: String
  ): ConfigObject = {
    val origin = valueOrigin(originDescription)
    fromAnyRef(pathMap, origin, FromMapMode.KEYS_ARE_PATHS)
      .asInstanceOf[ConfigObject]
  }

  def fromAnyRef(
      obj: Any,
      origin: ConfigOrigin,
      mapMode: FromMapMode
  ): AbstractConfigValue = {
    if (origin == null)
      throw new ConfigException.BugOrBroken("origin not supposed to be null")
    if (obj == null)
      if (origin != defaultValueOrigin)
        new ConfigNull(origin)
      else defaultNullValue
    else if (obj.isInstanceOf[AbstractConfigValue])
      obj.asInstanceOf[AbstractConfigValue]
    else if (obj.isInstanceOf[jl.Boolean])
      if (origin != defaultValueOrigin)
        new ConfigBoolean(origin, obj.asInstanceOf[jl.Boolean])
      else if (obj.asInstanceOf[jl.Boolean]) defaultTrueValue
      else defaultFalseValue
    else if (obj.isInstanceOf[String])
      new ConfigString.Quoted(origin, obj.asInstanceOf[String])
    else if (obj.isInstanceOf[Number]) {
      // here we always keep the same type that was passed to us,
      // rather than figuring out if a Long would fit in an Int
      // or a Double has no fractional part. i.e. deliberately
      // not using ConfigNumber.newNumber() when we have a
      // Double, Integer, or Long.
      if (obj.isInstanceOf[jl.Double])
        new ConfigDouble(origin, obj.asInstanceOf[jl.Double], null)
      else if (obj.isInstanceOf[Integer])
        new ConfigInt(origin, obj.asInstanceOf[Integer], null)
      else if (obj.isInstanceOf[jl.Long])
        new ConfigLong(origin, obj.asInstanceOf[jl.Long], null)
      else
        ConfigNumber.newNumber(
          origin,
          obj.asInstanceOf[Number].doubleValue,
          null
        )
    } else if (obj.isInstanceOf[Duration]) {
      new ConfigLong(origin, obj.asInstanceOf[Duration].toMillis, null)
    } else if (obj.isInstanceOf[ju.Map[_, _]]) {
      if (obj.asInstanceOf[ju.Map[_, _]].isEmpty)
        return emptyObject(origin)
      if (mapMode == FromMapMode.KEYS_ARE_KEYS) {
        val values = new ju.HashMap[String, AbstractConfigValue]
        for (entry <- obj.asInstanceOf[ju.Map[_, _]].entrySet.asScala) {
          val key = entry.getKey
          if (!key.isInstanceOf[String])
            throw new ConfigException.BugOrBroken(
              "bug in method caller: not valid to create ConfigObject from map with non-String key: " + key
            )
          val value = fromAnyRef(entry.getValue, origin, mapMode)
          values.put(key.asInstanceOf[String], value)
        }
        new SimpleConfigObject(origin, values)
      } else {
        PropertiesParser.fromPathMap(origin, obj.asInstanceOf[ju.Map[_, _]])
      }
    } else if (obj.isInstanceOf[jl.Iterable[_]]) {
      val i = obj.asInstanceOf[jl.Iterable[_]].iterator
      if (!i.hasNext) return emptyList(origin)
      val values = new ju.ArrayList[AbstractConfigValue]
      while (i.hasNext) {
        val v = fromAnyRef(i.next, origin, mapMode)
        values.add(v)
      }
      new SimpleConfigList(origin, values)
    } else if (obj.isInstanceOf[ConfigMemorySize]) {
      new ConfigLong(origin, obj.asInstanceOf[ConfigMemorySize].toBytes, null)
    } else {
      throw new ConfigException.BugOrBroken(
        "bug in method caller: not valid to create ConfigValue from: " + obj
      )
    }
  }

  private object DefaultIncluderHolder {
    private[impl] val defaultIncluder = new SimpleIncluder(null)
  }

  private[impl] def defaultIncluder: ConfigIncluder =
    // this calls a simple constructor - not sure why we are catching this
    try DefaultIncluderHolder.defaultIncluder
    catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }

  private def getSystemProperties: ju.Properties = {
    // Avoid ConcurrentModificationException due to parallel setting of system properties by copying properties
    val systemProperties     = System.getProperties
    val systemPropertiesCopy = new ju.Properties
    systemProperties.synchronized {
      for (entry <- systemProperties.entrySet().asScala) {
        // Java 11 introduces 'java.version.date', but we don't want that to
        // overwrite 'java.version'
        if (!entry.getKey().toString().startsWith("java.version.")) {
          systemPropertiesCopy.put(entry.getKey(), entry.getValue());
        }
      }
    }
    systemPropertiesCopy
  }

  private def loadSystemProperties: AbstractConfigObject =
    Parseable
      .newProperties(
        getSystemProperties,
        ConfigParseOptions.defaults.setOriginDescription("system properties")
      )
      .parse()
      .asInstanceOf[AbstractConfigObject]

  private object SystemPropertiesHolder {
    // this isn't final due to the reloadSystemPropertiesConfig() hack below
    @volatile private[impl] var systemProperties: AbstractConfigObject =
      loadSystemProperties
  }

  private[impl] def systemPropertiesAsConfigObject: AbstractConfigObject =
    try {
      SystemPropertiesHolder.systemProperties
    } catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }

  def systemPropertiesAsConfig: Config =
    systemPropertiesAsConfigObject.toConfig

  def reloadSystemPropertiesConfig(): Unit = {
    // ConfigFactory.invalidateCaches() relies on this having the side
    // effect that it drops all caches
    // change - could not find the side effect so added a method to invalidate explicitly
    LoaderCacheHolder.invalidate()
    SystemPropertiesHolder.systemProperties = loadSystemProperties
  }

  private def loadEnvVariables: AbstractConfigObject =
    PropertiesParser.fromStringMap(
      newSimpleOrigin("env variables"),
      System.getenv
    )

  private object EnvVariablesHolder {
    @volatile private[impl] var envVariables = loadEnvVariables
  }

  def envVariablesAsConfigObject: AbstractConfigObject =
    try {
      EnvVariablesHolder.envVariables
    } catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }

  def envVariablesAsConfig: Config = envVariablesAsConfigObject.toConfig

  def reloadEnvVariablesConfig(): Unit = {
    // ConfigFactory.invalidateCaches() relies on this having the side
    // effect that it drops all caches
    EnvVariablesHolder.envVariables = loadEnvVariables
  }

  def defaultReference(loader: ClassLoader): Config = {
    val updater = new Callable[Config] {
      override def call(): Config = {
        val unresolvedResources = Parseable
          .newResources(
            "reference.conf",
            ConfigParseOptions.defaults.setClassLoader(loader)
          )
          .parse()
          .toConfig
        val config = systemPropertiesAsConfig
          .withFallback(unresolvedResources)
          .resolve()
        config
      }
    }
    computeCachedConfig(loader, "defaultReference", updater)
  }

  private object DebugHolder {
    private val LOADS         = "loads"
    private val SUBSTITUTIONS = "substitutions"
    private def loadDiagnostics: ju.Map[String, jl.Boolean] = {
      val result = new ju.HashMap[String, jl.Boolean]
      result.put(LOADS, false)
      result.put(SUBSTITUTIONS, false)
      // People do -Dconfig.trace=foo,bar to enable tracing of different things
      val s = System.getProperty("config.trace")
      if (s == null) result
      else {
        val keys = s.split(",")
        for (k <- keys) {
          if (k == LOADS)
            result.put(LOADS, true)
          else if (k == SUBSTITUTIONS)
            result.put(SUBSTITUTIONS, true)
          else
            System.err.println(
              "config.trace property contains unknown trace topic '" + k + "'"
            )
        }
        result
      }
    }
    private[impl] val diagnostics: ju.Map[String, jl.Boolean] = loadDiagnostics
    private[impl] val traceLoadsEnabled: Boolean              = diagnostics.get(LOADS)
    private[impl] val traceSubstitutionsEnabled: Boolean =
      diagnostics.get(SUBSTITUTIONS)
    // two methods for the two previous vals are not needed in Scala
  }

  def traceLoadsEnabled: Boolean =
    try {
      DebugHolder.traceLoadsEnabled
    } catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }
  def traceSubstitutionsEnabled: Boolean =
    try {
      DebugHolder.traceSubstitutionsEnabled
    } catch {
      case e: ExceptionInInitializerError =>
        throw ConfigImplUtil.extractInitializerError(e)
    }
  def trace(message: String): Unit = System.err.println(message)
  def trace(indentLevel: Int, message: String): Unit = {
    var level = indentLevel
    while (level > 0) {
      System.err.print("  ")
      level -= 1
    }
    System.err.println(message)
  }
  // the basic idea here is to add the "what" and have a canonical
  // toplevel error message. the "original" exception may however have extra
  // detail about what happened. call this if you have a better "what" than
  // further down on the stack.
  def improveNotResolved(
      what: Path,
      original: ConfigException.NotResolved
  ): ConfigException.NotResolved = {
    val newMessage = what.render +
      " has not been resolved, you need to call Config#resolve()," +
      " see API docs for Config#resolve()"
    if (newMessage == original.getMessage) original
    else new ConfigException.NotResolved(newMessage, original)
  }
  def newSimpleOrigin(description: String): ConfigOrigin =
    if (description == null) defaultValueOrigin
    else SimpleConfigOrigin.newSimple(description)
  def newFileOrigin(filename: String): ConfigOrigin =
    SimpleConfigOrigin.newFile(filename)
  def newURLOrigin(url: URL): ConfigOrigin = SimpleConfigOrigin.newURL(url)
}
