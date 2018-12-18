/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config

import org.ekrich.config.impl.ConfigImpl
import org.ekrich.config.impl.Parseable
import java.io.{File, Reader}
import java.net.URL
import java.{util => ju}
import java.util.Properties
import java.util.concurrent.Callable

/**
 * Contains static methods for creating [[Config]] instances.
 *
 * See also [[ConfigValueFactory]] which contains static methods for
 * converting Java values into a [[ConfigObject]]. You can then convert a
 * [[ConfigObject]] into a [[Config]] with [[ConfigObject#toConfig]].
 *
 * The static methods with "load" in the name do some sort of higher-level
 * operation potentially parsing multiple resources and resolving substitutions,
 * while the ones with "parse" in the name just create a [[ConfigValue]]
 * from a resource and nothing else.
 *
 * You can find an example app and library <a
 * [[https://github.com/ekrich/sconfig/tree/master/examples on GitHub]].
 * Also be sure to read the <a
 * href="package-summary.html#package_description">package
 * overview</a> which describes the big picture as shown in those
 * examples.
 */
object ConfigFactory {
  private val STRATEGY_PROPERTY_NAME = "config.strategy"

  /**
   * Loads an application's configuration from the given classpath resource or
   * classpath resource basename, sandwiches it between default reference
   * config and default overrides, and then resolves it. The classpath
   * resource is "raw" (it should have no "/" prefix, and is not made relative
   * to any package, so it's like [[ClassLoader#getResource]] not
   * [[Class#getResource]]).
   *
   * Resources are loaded from the current thread's
   * [[Thread!.getContextClassLoader()]]. In general, a library needs its
   * configuration to come from the class loader used to load that library, so
   * the proper "reference.conf" are present.
   *
   * The loaded object will already be resolved (substitutions have already
   * been processed). As a result, if you add more fallbacks then they won't
   * be seen by substitutions. Substitutions are the `\${foo.bar}` syntax. If
   * you want to parse additional files or something then you need to use
   * [[ConfigFactory.load(Config)]].
   *
   * To load a standalone resource (without the default reference and default
   * overrides), use [[ConfigFactory.parseResourcesAnySyntax(String)]] rather than this
   * method. To load only the reference config use [[ConfigFactory.defaultReference()]]
   * and to load only the overrides use [[ConfigFactory.defaultOverrides()]].
   *
   * @param resourceBasename
   *            name (optionally without extension) of a resource on classpath
   * @return configuration for an application relative to context class loader
   */
  def load(resourceBasename: String): Config =
    load(resourceBasename,
         ConfigParseOptions.defaults,
         ConfigResolveOptions.defaults)

  /**
   * Like [[ConfigFactory.load(String)]] but uses the supplied class loader instead of
   * the current thread's context class loader.
   *
   * To load a standalone resource (without the default reference and default
   * overrides), use [[ConfigFactory.parseResourcesAnySyntax(ClassLoader, String)]]
   * rather than this method. To load only the reference config use
   * [[ConfigFactory.defaultReference(ClassLoader)]] and to load only the overrides use
   * [[ConfigFactory.defaultOverrides(ClassLoader)]].
   *
   * @param loader class loader to look for resources in
   * @param resourceBasename basename (no .conf/.json/.properties suffix)
   * @return configuration for an application relative to given class loader
   */
  def load(loader: ClassLoader, resourceBasename: String): Config =
    load(resourceBasename,
         ConfigParseOptions.defaults.setClassLoader(loader),
         ConfigResolveOptions.defaults)

  /**
   * Like [[ConfigFactory.load(String)]] but allows you to specify parse and resolve
   * options.
   *
   * @param resourceBasename
   *            the classpath resource name with optional extension
   * @param parseOptions
   *            options to use when parsing the resource
   * @param resolveOptions
   *            options to use when resolving the stack
   * @return configuration for an application
   */
  def load(resourceBasename: String,
           parseOptions: ConfigParseOptions,
           resolveOptions: ConfigResolveOptions): Config = {
    val withLoader = ensureClassLoader(parseOptions, "load")
    val appConfig =
      ConfigFactory.parseResourcesAnySyntax(resourceBasename, withLoader)
    load(withLoader.getClassLoader, appConfig, resolveOptions)
  }

  /**
   * Like [[ConfigFactory.load(String,ConfigParseOptions,ConfigResolveOptions)]] but
   * has a class loader parameter that overrides any from the
   * [[ConfigParseOptions]].
   *
   * @param loader
   *            class loader in which to find resources (overrides loader in
   *            parse options)
   * @param resourceBasename
   *            the classpath resource name with optional extension
   * @param parseOptions
   *            options to use when parsing the resource (class loader
   *            overridden)
   * @param resolveOptions
   *            options to use when resolving the stack
   * @return configuration for an application
   */
  def load(loader: ClassLoader,
           resourceBasename: String,
           parseOptions: ConfigParseOptions,
           resolveOptions: ConfigResolveOptions): Config =
    load(resourceBasename, parseOptions.setClassLoader(loader), resolveOptions)
  private def checkedContextClassLoader(methodName: String) = {
    val loader = Thread.currentThread.getContextClassLoader
    if (loader == null)
      throw new ConfigException.BugOrBroken(
        "Context class loader is not set for the current thread; " + "if Thread.currentThread.getContextClassLoader returns null, you must pass a ClassLoader " + "explicitly to ConfigFactory." + methodName)
    else loader
  }
  private def ensureClassLoader(options: ConfigParseOptions,
                                methodName: String) =
    if (options.getClassLoader == null)
      options.setClassLoader(checkedContextClassLoader(methodName))
    else options

  /**
   * Assembles a standard configuration using a custom `Config`
   * object rather than loading "application.conf". The `Config`
   * object will be sandwiched between the default reference config and
   * default overrides and then resolved.
   *
   * @param config
   *            the application's portion of the configuration
   * @return resolved configuration with overrides and fallbacks added
   */
  def load(config: Config): Config =
    load(checkedContextClassLoader("load"), config)

  /**
   * Like [[ConfigFactory.load(Config)]] but allows you to specify
   * the class loader for looking up resources.
   *
   * @param loader
   *            the class loader to use to find resources
   * @param config
   *            the application's portion of the configuration
   * @return resolved configuration with overrides and fallbacks added
   */
  def load(loader: ClassLoader, config: Config): Config =
    load(loader, config, ConfigResolveOptions.defaults)

  /**
   * Like [[ConfigFactory.load(Config)]] but allows you to specify
   * [[ConfigResolveOptions]].
   *
   * @param config
   *            the application's portion of the configuration
   * @param resolveOptions
   *            options for resolving the assembled config stack
   * @return resolved configuration with overrides and fallbacks added
   */
  def load(config: Config, resolveOptions: ConfigResolveOptions): Config =
    load(checkedContextClassLoader("load"), config, resolveOptions)

  /**
   * Like [[ConfigFactory.load(Config,ConfigResolveOptions)]] but allows you to specify
   * a class loader other than the context class loader.
   *
   * @param loader
   *            class loader to use when looking up override and reference
   *            configs
   * @param config
   *            the application's portion of the configuration
   * @param resolveOptions
   *            options for resolving the assembled config stack
   * @return resolved configuration with overrides and fallbacks added
   */
  def load(loader: ClassLoader,
           config: Config,
           resolveOptions: ConfigResolveOptions): Config =
    defaultOverrides(loader)
      .withFallback(config)
      .withFallback(defaultReference(loader))
      .resolve(resolveOptions)

  /**
   * Loads a default configuration, equivalent to [[ConfigFactory.load(Config)]]
   * [[.load(defaultApplication())]] in most cases. This configuration should be used by
   * libraries and frameworks unless an application provides a different one.
   *
   * This method may return a cached singleton so will not see changes to
   * system properties or config files. (Use [[ConfigFactory.invalidateCaches()]] to
   * force it to reload.)
   *
   * @return configuration for an application
   */
  def load(): Config = {
    val loader = checkedContextClassLoader("load")
    load(loader)
  }

  /**
   * Like [[ConfigFactory.load()]] but allows specifying parse options.
   *
   * @param parseOptions
   *            Options for parsing resources
   * @return configuration for an application
   */
  def load(parseOptions: ConfigParseOptions): Config =
    load(parseOptions, ConfigResolveOptions.defaults)

  /**
   * Like [[ConfigFactory.load()]] but allows specifying a class loader other than the
   * thread's current context class loader.
   *
   * @param loader
   *            class loader for finding resources
   * @return configuration for an application
   */
  def load(loader: ClassLoader): Config = {
    val withLoader =
      ConfigParseOptions.defaults.setClassLoader(loader)
    ConfigImpl.computeCachedConfig(loader, "load", new Callable[Config]() {
      override def call: Config =
        return load(loader, defaultApplication(withLoader))
    })
  }

  /**
   * Like [[ConfigFactory.load()]] but allows specifying a class loader other than the
   * thread's current context class loader and also specify parse options.
   *
   * @param loader
   *            class loader for finding resources (overrides any loader in parseOptions)
   * @param parseOptions
   *            Options for parsing resources
   * @return configuration for an application
   */
  def load(loader: ClassLoader, parseOptions: ConfigParseOptions): Config =
    load(parseOptions.setClassLoader(loader))

  /**
   * Like [[ConfigFactory.load()]] but allows specifying a class loader other than the
   * thread's current context class loader and also specify resolve options.
   *
   * @param loader
   *            class loader for finding resources
   * @param resolveOptions
   *            options for resolving the assembled config stack
   * @return configuration for an application
   */
  def load(loader: ClassLoader, resolveOptions: ConfigResolveOptions): Config =
    load(loader, ConfigParseOptions.defaults, resolveOptions)

  /**
   * Like [[ConfigFactory.load()]] but allows specifying a class loader other than the
   * thread's current context class loader, parse options, and resolve options.
   *
   * @param loader
   *            class loader for finding resources (overrides any loader in parseOptions)
   * @param parseOptions
   *            Options for parsing resources
   * @param resolveOptions
   *            options for resolving the assembled config stack
   * @return configuration for an application
   */
  def load(loader: ClassLoader,
           parseOptions: ConfigParseOptions,
           resolveOptions: ConfigResolveOptions): Config = {
    val withLoader = ensureClassLoader(parseOptions, "load")
    load(loader, defaultApplication(withLoader), resolveOptions)
  }

  /**
   * Like [[ConfigFactory.load()]] but allows specifying parse options and resolve
   * options.
   *
   * @param parseOptions
   *            Options for parsing resources
   * @param resolveOptions
   *            options for resolving the assembled config stack
   * @return configuration for an application
   * @since 1.3.0
   */
  def load(parseOptions: ConfigParseOptions,
           resolveOptions: ConfigResolveOptions): Config = {
    val withLoader = ensureClassLoader(parseOptions, "load")
    load(defaultApplication(withLoader), resolveOptions)
  }

  /**
   * Obtains the default reference configuration, which is currently created
   * by merging all resources "reference.conf" found on the classpath and
   * overriding the result with system properties. The returned reference
   * configuration will already have substitutions resolved.
   *
   * Libraries and frameworks should ship with a "reference.conf" in their
   * jar.
   *
   * The reference config must be looked up in the class loader that contains
   * the libraries that you want to use with this config, so the
   * "reference.conf" for each library can be found. Use
   * [[ConfigFactory.defaultReference(ClassLoader)]] if the context class loader is not
   * suitable.
   *
   * The [[ConfigFactory.load()]] methods merge this configuration for you
   * automatically.
   *
   * Future versions may look for reference configuration in more places. It
   * is not guaranteed that this method ''only'' looks at
   * "reference.conf".
   *
   * @return the default reference config for context class loader
   */
  def defaultReference(): Config =
    defaultReference(checkedContextClassLoader("defaultReference"))

  /**
   * Like [[ConfigFactory.defaultReference()]] but allows you to specify a class loader
   * to use rather than the current context class loader.
   *
   * @param loader class loader to look for resources in
   * @return the default reference config for this class loader
   */
  def defaultReference(loader: ClassLoader): Config =
    ConfigImpl.defaultReference(loader)

  /**
   * Obtains the default override configuration, which currently consists of
   * system properties. The returned override configuration will already have
   * substitutions resolved.
   *
   * The [[ConfigFactory.load()]] methods merge this configuration for you
   * automatically.
   *
   * Future versions may get overrides in more places. It is not guaranteed
   * that this method <em>only</em> uses system properties.
   *
   * @return the default override configuration
   */
  def defaultOverrides(): Config = systemProperties

  /**
   * Like [[ConfigFactory.defaultOverrides()]] but allows you to specify a class loader
   * to use rather than the current context class loader.
   *
   * @param loader class loader to look for resources in
   * @return the default override configuration
   */
  def defaultOverrides(loader: ClassLoader): Config = systemProperties

  /**
   * Obtains the default application-specific configuration,
   * which defaults to parsing `application.conf`,
   * `application.json`, and
   * `application.properties` on the classpath, but
   * can also be rerouted using the `config.file`,
   * `config.resource`, and `config.url`
   * system properties.
   *
   * The no-arguments [[ConfigFactory.load()]] method automatically
   * stacks the [[ConfigFactory.defaultReference()]], [[ConfigFactory.defaultApplication()]], and [[ConfigFactory.defaultOverrides()]]
   * configs. You would use `defaultApplication()`
   * directly only if you're somehow customizing behavior by
   * reimplementing `load()`.
   *
   * The configuration returned by
   * `defaultApplication()` will not be resolved
   * already, in contrast to `defaultReference()` and
   * `defaultOverrides()`. This is because
   * application.conf would normally be resolved ''after''
   * merging with the reference and override configs.
   *
   * If the system properties `config.resource`,
   * `config.file`, or `config.url` are set, then the
   * classpath resource, file, or URL specified in those properties will be
   * used rather than the default
   * `application.{conf,json,properties]]` classpath resources.
   * These system properties should not be set in code (after all, you can
   * just parse whatever you want manually and then use [[ConfigFactory.load(Config)]]
   * if you don't want to use `application.conf`). The properties
   * are intended for use by the person or script launching the application.
   * For example someone might have a `production.conf` that
   * include `application.conf` but then change a couple of values.
   * When launching the app they could specify
   * `-Dconfig.resource=production.conf` to get production mode.
   *
   * If no system properties are set to change the location of the default
   * configuration, `defaultApplication()` is equivalent to
   * `ConfigFactory.parseResources("application")`.
   *
   * @since 1.3.0
   * @return the default application.conf or system-property-configured configuration
   */
  def defaultApplication(): Config =
    defaultApplication(ConfigParseOptions.defaults)

  /**
   * Like [[ConfigFactory.defaultApplication()]] but allows you to specify a class loader
   * to use rather than the current context class loader.
   *
   * @since 1.3.0
   * @param loader class loader to look for resources in
   * @return the default application configuration
   */
  def defaultApplication(loader: ClassLoader): Config =
    defaultApplication(ConfigParseOptions.defaults.setClassLoader(loader))

  /**
   * Like [[ConfigFactory.defaultApplication()]] but allows you to specify parse options.
   *
   * @since 1.3.0
   * @param options the options
   * @return the default application configuration
   */
  def defaultApplication(options: ConfigParseOptions): Config =
    getConfigLoadingStrategy.parseApplicationConfig(
      ensureClassLoader(options, "defaultApplication"))

  /**
   * Reloads any cached configs, picking up changes to system properties for
   * example. Because a [[Config]] is immutable, anyone with a reference
   * to the old configs will still have the same outdated objects. However,
   * new calls to [[ConfigFactory.load()]] or [[ConfigFactory.defaultOverrides()]] or
   * [[ConfigFactory.defaultReference]] may return a new object.
   *
   * This method is primarily intended for use in unit tests, for example,
   * that may want to update a system property then confirm that it's used
   * correctly. In many cases, use of this method may indicate there's a
   * better way to set up your code.
   *
   * Caches may be reloaded immediately or lazily; once you call this method,
   * the reload can occur at any time, even during the invalidation process.
   * So FIRST make the changes you'd like the caches to notice, then SECOND
   * call this method to invalidate caches. Don't expect that invalidating,
   * making changes, then calling [[ConfigFactory.load()]], will work. Make changes
   * before you invalidate.
   */
  def invalidateCaches(): Unit = {
    // We rely on this having the side effect that it drops all caches
    ConfigImpl.reloadSystemPropertiesConfig()
    ConfigImpl.reloadEnvVariablesConfig()
  }

  /**
   * Gets an empty configuration. See also [[ConfigFactory.empty(String)]] to create an
   * empty configuration with a description, which may improve user-visible
   * error messages.
   *
   * @return an empty configuration
   */
  def empty(): Config = empty(null)

  /**
   * Gets an empty configuration with a description to be used to create a
   * [[ConfigOrigin]] for this `Config`. The description should
   * be very short and say what the configuration is, like "default settings"
   * or "foo settings" or something. (Presumably you will merge some actual
   * settings into this empty config using [[Config.withFallback]], making
   * the description more useful.)
   *
   * @param originDescription
   *            description of the config
   * @return an empty configuration
   */
  def empty(originDescription: String): Config =
    ConfigImpl.emptyConfig(originDescription)

  /**
   * Gets a `Config` containing the system properties from
   * [[java.lang.System#getProperties()]], parsed and converted as with
   * [[ConfigFactory#parseProperties]].
   *
   * This method can return a global immutable singleton, so it's preferred
   * over parsing system properties yourself.
   *
   * [[ConfigFactory#load]] will include the system properties as overrides already, as
   * will [[ConfigFactory#defaultReference]] and [[ConfigFactory#defaultOverrides]].
   *
   * Because this returns a singleton, it will not notice changes to system
   * properties made after the first time this method is called. Use
   * [[ConfigFactory#invalidateCaches()]] to force the singleton to reload if you
   * modify system properties.
   *
   * @return system properties parsed into a [[Config]]
   */
  def systemProperties(): Config = ConfigImpl.systemPropertiesAsConfig

  /**
   * Gets a [[Config]] containing the system's environment variables.
   * This method can return a global immutable singleton.
   *
   * Environment variables are used as fallbacks when resolving substitutions
   * whether or not this object is included in the config being resolved, so
   * you probably don't need to use this method for most purposes. It can be a
   * nicer API for accessing environment variables than raw
   * [[java.lang.System.getenv(String)]] though, since you can use methods
   * such as [[Config.getInt]].
   *
   * @return system environment variables parsed into a `Config`
   */
  def systemEnvironment(): Config = ConfigImpl.envVariablesAsConfig

  /**
   * Converts a Java [[java.util.Properties]] object to a
   * [[ConfigObject]] using the rules documented in the
   * [[https://github.com/ekrich/sconfig/blob/master/HOCON.md HOCON spec]]
   * The keys in the `Properties` object are split on the
   * period character '.' and treated as paths. The values will all end up as
   * string values. If you have both "a=foo" and "a.b=bar" in your properties
   * file, so "a" is both the object containing "b" and the string "foo", then
   * the string value is dropped.
   *
   * If you want to have `System.getProperties()` as a
   * ConfigObject, it's better to use the [[ConfigFactory.systemProperties()]] method
   * which returns a cached global singleton.
   *
   * @param properties
   *            a Java Properties object
   * @param options
   *            the parse options
   * @return the parsed configuration
   */
  def parseProperties(properties: Properties,
                      options: ConfigParseOptions): Config =
    Parseable.newProperties(properties, options).parse.toConfig

  /**
   * Like [[ConfigFactory#parseProperties(Properties, ConfigParseOptions)]] but uses default
   * parse options.
   *
   * @param properties
   *            a Java Properties object
   * @return the parsed configuration
   */
  def parseProperties(properties: Properties): Config =
    parseProperties(properties, ConfigParseOptions.defaults)

  /**
   * Parses a Reader into a Config instance. Does not call
   * [[Config.resolve*]] or merge the parsed stream with any
   * other configuration; this method parses a single stream and
   * does nothing else. It does process "include" statements in
   * the parsed stream, and may end up doing other IO due to those
   * statements.
   *
   * @param reader
   *       the reader to parse
   * @param options
   *       parse options to control how the reader is interpreted
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseReader(reader: Reader, options: ConfigParseOptions): Config =
    Parseable.newReader(reader, options).parse.toConfig

  /**
   * Parses a reader into a Config instance as with
   * [[ConfigFactory.parseReader(Reader,ConfigParseOptions)]] but always uses the
   * default parse options.
   *
   * @param reader
   *       the reader to parse
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseReader(reader: Reader): Config =
    parseReader(reader, ConfigParseOptions.defaults)

  /**
   * Parses a URL into a Config instance. Does not call
   * [[Config.resolve*]] or merge the parsed stream with any
   * other configuration; this method parses a single stream and
   * does nothing else. It does process "include" statements in
   * the parsed stream, and may end up doing other IO due to those
   * statements.
   *
   * @param url
   *       the url to parse
   * @param options
   *       parse options to control how the url is interpreted
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseURL(url: URL, options: ConfigParseOptions): Config =
    Parseable.newURL(url, options).parse.toConfig

  /**
   * Parses a url into a [[Config]] instance as with
   * [[ConfigFactory.parseURL(URL,ConfigParseOptions)]]
   * but always uses the default parse options.
   *
   * @param url
   *       the url to parse
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseURL(url: URL): Config = parseURL(url, ConfigParseOptions.defaults)

  /**
   * Parses a file into a Config instance. Does not call
   * [[Config.resolve]] or merge the file with any other
   * configuration; this method parses a single file and does
   * nothing else. It does process "include" statements in the
   * parsed file, and may end up doing other IO due to those
   * statements.
   *
   * @param file
   *       the file to parse
   * @param options
   *       parse options to control how the file is interpreted
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseFile(file: File, options: ConfigParseOptions): Config =
    Parseable.newFile(file, options).parse.toConfig

  /**
   * Parses a file into a Config instance as with
   * [[ConfigFactory#parseFile(File,ConfigParseOptions)]] but always uses the
   * default parse options.
   *
   * @param file
   *       the file to parse
   * @return the parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseFile(file: File): Config =
    parseFile(file, ConfigParseOptions.defaults)

  /**
   * Parses a file with a flexible extension. If the `fileBasename`
   * already ends in a known extension, this method parses it according to
   * that extension (the file's syntax must match its extension). If the
   * `fileBasename` does not end in an extension, it parses files
   * with all known extensions and merges whatever is found.
   *
   * In the current implementation, the extension ".conf" forces
   * [[ConfigSyntax.CONF]], ".json" forces [[ConfigSyntax.JSON]], and
   * ".properties" forces [[ConfigSyntax.PROPERTIES]]. When merging files,
   * ".conf" falls back to ".json" falls back to ".properties".
   *
   * Future versions of the implementation may add additional syntaxes or
   * additional extensions. However, the ordering (fallback priority) of the
   * three current extensions will remain the same.
   *
   * If `options` forces a specific syntax, this method only parses
   * files with an extension matching that syntax.
   *
   * If [[ConfigParseOptions#getAllowMissing]]
   * is true, then no files have to exist; if false, then at least one file
   * has to exist.
   *
   * @param fileBasename
   *            a filename with or without extension
   * @param options
   *            parse options
   * @return the parsed configuration
   */
  def parseFileAnySyntax(fileBasename: File,
                         options: ConfigParseOptions): Config =
    ConfigImpl.parseFileAnySyntax(fileBasename, options).toConfig

  /**
   * Like [[ConfigFactory#parseFileAnySyntax(File,ConfigParseOptions)]] but always uses
   * default parse options.
   *
   * @param fileBasename
   *            a filename with or without extension
   * @return the parsed configuration
   */
  def parseFileAnySyntax(fileBasename: File): Config =
    parseFileAnySyntax(fileBasename, ConfigParseOptions.defaults)

  /**
   * Parses all resources on the classpath with the given name and merges them
   * into a single [[Config]].
   *
   * If the resource name does not begin with a "/", it will have the supplied
   * class's package added to it, in the same way as
   * [[java.lang.Class#getResource]].
   *
   * Duplicate resources with the same name are merged such that ones returned
   * earlier from [[ClassLoader#getResources]] fall back to (have higher
   * priority than) the ones returned later# This implies that resources
   * earlier in the classpath override those later in the classpath when they
   * configure the same setting# However, in practice real applications may
   * not be consistent about classpath ordering, so be careful# It may be best
   * to avoid assuming too much#
   *
   * @param klass
   *            `klass.getClassLoader()` will be used to load
   *            resources, and non-absolute resource names will have this
   *            class's package added
   * @param resource
   *            resource to look up, relative to `klass`'s package
   *            or absolute starting with a "/"
   * @param options
   *            parse options
   * @return the parsed configuration
   */
  def parseResources(klass: Class[_],
                     resource: String,
                     options: ConfigParseOptions): Config =
    Parseable.newResources(klass, resource, options).parse.toConfig

  /**
   * Like [[ConfigFactory#parseResources(Class,String,ConfigParseOptions)]] but always uses
   * default parse options.
   *
   * @param klass
   *            `klass.getClassLoader()` will be used to load
   *            resources, and non-absolute resource names will have this
   *            class's package added
   * @param resource
   *            resource to look up, relative to `klass`'s package
   *            or absolute starting with a "/"
   * @return the parsed configuration
   */
  def parseResources(klass: Class[_], resource: String): Config =
    parseResources(klass, resource, ConfigParseOptions.defaults)

  /**
   * Parses classpath resources with a flexible extension. In general, this
   * method has the same behavior as
   * [[ConfigFactory#parseFileAnySyntax(File,ConfigParseOptions)]] but for classpath
   * resources instead, as in [[ConfigFactory#parseResources]].
   *
   * There is a thorny problem with this method, which is that
   * [[java.lang.ClassLoader#getResources]] must be called separately for
   * each possible extension. The implementation ends up with separate lists
   * of resources called "basename.conf" and "basename.json" for example. As a
   * result, the ideal ordering between two files with different extensions is
   * unknown; there is no way to figure out how to merge the two lists in
   * classpath order. To keep it simple, the lists are simply concatenated,
   * with the same syntax priorities as
   * [[ConfigFactory#parseFileAnySyntax(File,ConfigParseOptions)]]
   * - all ".conf" resources are ahead of all ".json" resources which are
   * ahead of all ".properties" resources.
   *
   * @param klass
   *            class which determines the `ClassLoader` and the
   *            package for relative resource names
   * @param resourceBasename
   *            a resource name as in [[java.lang.Class#getResource]],
   *            with or without extension
   * @param options
   *            parse options (class loader is ignored in favor of the one
   *            from klass)
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(klass: Class[_],
                              resourceBasename: String,
                              options: ConfigParseOptions): Config =
    ConfigImpl
      .parseResourcesAnySyntax(klass, resourceBasename, options)
      .toConfig

  /**
   * Like [[ConfigFactory#parseResourcesAnySyntax(Class,String,ConfigParseOptions)]]
   * but always uses default parse options.
   *
   * @param klass
   *            `klass.getClassLoader()` will be used to load
   *            resources, and non-absolute resource names will have this
   *            class's package added
   * @param resourceBasename
   *            a resource name as in [[java.lang.Class#getResource]],
   *            with or without extension
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(klass: Class[_],
                              resourceBasename: String): Config =
    parseResourcesAnySyntax(klass,
                            resourceBasename,
                            ConfigParseOptions.defaults)

  /**
   * Parses all resources on the classpath with the given name and merges them
   * into a single `Config`.
   *
   * This works like [[java.lang.ClassLoader#getResource]], not like
   * [[java.lang.Class#getResource]], so the name never begins with a
   * slash.
   *
   * See [[ConfigFactory#parseResources(Class,String,ConfigParseOptions)]] for full
   * details.
   *
   * @param loader
   *            will be used to load resources by setting this loader on the
   *            provided options
   * @param resource
   *            resource to look up
   * @param options
   *            parse options (class loader is ignored)
   * @return the parsed configuration
   */
  def parseResources(loader: ClassLoader,
                     resource: String,
                     options: ConfigParseOptions): Config =
    parseResources(resource, options.setClassLoader(loader))

  /**
   * Like [[ConfigFactory#parseResources(ClassLoader,String,ConfigParseOptions)]] but always uses
   * default parse options.
   *
   * @param loader
   *            will be used to load resources
   * @param resource
   *            resource to look up in the loader
   * @return the parsed configuration
   */
  def parseResources(loader: ClassLoader, resource: String): Config =
    parseResources(loader, resource, ConfigParseOptions.defaults)

  /**
   * Parses classpath resources with a flexible extension. In general, this
   * method has the same behavior as
   * [[ConfigFactory#parseFileAnySyntax(File,ConfigParseOptions)]] but for classpath
   * resources instead, as in
   * [[ConfigFactory#parseResources(ClassLoader,String,ConfigParseOptions)]].
   *
   * [[ConfigFactory#parseResourcesAnySyntax(Class,String,ConfigParseOptions)]] differs
   * in the syntax for the resource name, but otherwise see
   * [[ConfigFactory#parseResourcesAnySyntax(Class,String,ConfigParseOptions)]] for
   * some details and caveats on this method.
   *
   * @param loader
   *            class loader to look up resources in, will be set on options
   * @param resourceBasename
   *            a resource name as in
   *            [[java.lang.ClassLoader#getResource]], with or without
   *            extension
   * @param options
   *            parse options (class loader ignored)
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(loader: ClassLoader,
                              resourceBasename: String,
                              options: ConfigParseOptions): Config =
    ConfigImpl
      .parseResourcesAnySyntax(resourceBasename, options.setClassLoader(loader))
      .toConfig

  /**
   * Like [[ConfigFactory#parseResourcesAnySyntax(ClassLoader,String,ConfigParseOptions)]] but always uses
   * default parse options.
   *
   * @param loader
   *            will be used to load resources
   * @param resourceBasename
   *            a resource name as in
   *            [[java.lang.ClassLoader#getResource]], with or without
   *            extension
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(loader: ClassLoader,
                              resourceBasename: String): Config =
    parseResourcesAnySyntax(loader,
                            resourceBasename,
                            ConfigParseOptions.defaults)

  /**
   * Like [[ConfigFactory#parseResources(ClassLoader,String,ConfigParseOptions)]] but
   * uses thread's current context class loader if none is set in the
   * ConfigParseOptions.
   *
   * @param resource the resource name
   * @param options parse options
   * @return the parsed configuration
   */
  def parseResources(resource: String, options: ConfigParseOptions): Config = {
    val withLoader =
      ensureClassLoader(options, "parseResources")
    Parseable.newResources(resource, withLoader).parse.toConfig
  }

  /**
   * Like [[ConfigFactory#parseResources(ClassLoader,String)]] but uses thread's
   * current context class loader.
   *
   * @param resource the resource name
   * @return the parsed configuration
   */
  def parseResources(resource: String): Config =
    parseResources(resource, ConfigParseOptions.defaults)

  /**
   * Like
   * [[ConfigFactory#parseResourcesAnySyntax(ClassLoader,String,ConfigParseOptions)]]
   * but uses thread's current context class loader.
   *
   * @param resourceBasename the resource basename (no file type suffix)
   * @param options parse options
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(resourceBasename: String,
                              options: ConfigParseOptions): Config =
    ConfigImpl
      .parseResourcesAnySyntax(resourceBasename, options)
      .toConfig

  /**
   * Like [[ConfigFactory#parseResourcesAnySyntax(ClassLoader,String)]] but uses
   * thread's current context class loader.
   *
   * @param resourceBasename the resource basename (no file type suffix)
   * @return the parsed configuration
   */
  def parseResourcesAnySyntax(resourceBasename: String): Config =
    parseResourcesAnySyntax(resourceBasename, ConfigParseOptions.defaults)

  /**
   * Parses a string (which should be valid HOCON or JSON by default, or
   * the syntax specified in the options otherwise).
   *
   * @param s string to parse
   * @param options parse options
   * @return the parsed configuration
   */
  def parseString(s: String, options: ConfigParseOptions): Config =
    Parseable.newString(s, options).parse.toConfig

  /**
   * Parses a string (which should be valid HOCON or JSON).
   *
   * @param s string to parse
   * @return the parsed configuration
   */
  def parseString(s: String): Config =
    parseString(s, ConfigParseOptions.defaults)

  /**
   * Creates a [[Config]] based on a [[java.util.Map]] from paths to
   * plain Java values. Similar to
   * [[ConfigValueFactory#fromMap(Map,String)]], except the keys in the
   * map are path expressions, rather than keys; and correspondingly it
   * returns a [[Config]] instead of a [[ConfigObject]]. This is more
   * convenient if you are writing literal maps in code, and less convenient
   * if you are getting your maps from some data source such as a parser.
   *
   * An exception will be thrown (and it is a bug in the caller of the method)
   * if a path is both an object and a value, for example if you had both
   * "a=foo" and "a.b=bar", then "a" is both the string "foo" and the parent
   * object of "b". The caller of this method should ensure that doesn't
   * happen.
   *
   * @param values map from paths to plain Java objects
   * @param originDescription
   *            description of what this map represents, like a filename, or
   *            "default settings" (origin description is used in error
   *            messages)
   * @return the map converted to a [[Config]]
   */
  def parseMap(values: ju.Map[String, _], originDescription: String): Config =
    ConfigImpl.fromPathMap(values, originDescription).toConfig

  /**
   * See the other overload of [[ConfigFactory#parseMap(Map, String)]] for details,
   * this one just uses a default origin description.
   *
   * @param values map from paths to plain Java values
   * @return the map converted to a [[Config]]
   */
  def parseMap(values: ju.Map[String, _]): Config = parseMap(values, null)

  private def getConfigLoadingStrategy = {
    val className =
      System.getProperties.getProperty(STRATEGY_PROPERTY_NAME)
    if (className != null)
      try classOf[ConfigLoadingStrategy].cast(
        Class.forName(className).getConstructor().newInstance())
      catch {
        case e: Throwable =>
          throw new ConfigException.BugOrBroken(
            "Failed to load strategy: " + className,
            e)
      } else new DefaultConfigLoadingStrategy
  }
}

final class ConfigFactory private () {}
