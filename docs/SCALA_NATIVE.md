# Scala Native and Scala.js Help

<kbd>[<- Back to README](../README.md)</kbd>

The release has minimal capabilities at this time so
the following is an example of how to use the API.

## Dependency for Scala Native and Scala.js

Use three percent signs `%%%` for sbt 1.x and `%%` for sbt 2.x instead for dependency resolution.

```scala
// sconfig uses "provided" so you have a choice of the java.time API you use
// Refer to your preferred java.time project for version to use
libraryDependencies ++= Seq(
  "org.ekrich" %% "sconfig" % "x.y.z",
  "org.ekrich" %% "sjavatime" % "a.b.c"
)
```
Please refer to the `sjavatime` [home page](https://github.com/ekrich/sjavatime)
for the current version.

## Formatting

[HOCON Formatting](NEW_FEATURES.md#hocon-formatting) is full featured and
works the same on Scala.js and Scala Native as on the JVM. A `ConfigDocument`
never loads `include` statements, so files with includes can be formatted;
the includes are kept as written.

## Includes

The library itself does not resolve `include` statements on Scala.js and Scala
Native. The include kinds differ, because Scala Native has a real `java.io`
while Scala.js has no file system at all:

| Include                       | Scala.js          | Scala Native      |
| ----------------------------- | ----------------- | ----------------- |
| `include "name.conf"`         | `ConfigException` | `ConfigException` |
| `include file("x.conf")`      | `ConfigException` | works             |
| `include url("file:...")`     | `ConfigException` | `ConfigException` |
| `include classpath("x.conf")` | `ConfigException` | `ConfigException` |

The `ConfigException` is a `ConfigException.Generic`, not a
`NotImplementedError`, so a caller can catch it, and its message names the way
out: `ConfigParseOptions.setIncluder`. `ConfigFactory.parseFile(file)` throws on
Scala.js for the same reason.

The class `java.net.URL` is not available on either platform, and a classpath
lookup requires `getResources(name: String): Enumeration[URL]`. On Scala Native
`java.io` is real, which is why `file()` includes work there.

### Combine configs with `withFallback`

Instead of using `include`, read each file yourself and combine the configs
with `withFallback`, where the first config wins:

```scala
val config = ConfigFactory
  .parseString(appConfStr)
  .withFallback(ConfigFactory.parseString(defaultsConfStr))
  .resolve()
```

### Resolve includes with your own `ConfigIncluder`

`ConfigParseOptions.setIncluder` makes includes work on every platform,
including a Scala.js program in a browser: the library calls your
`ConfigIncluder` and does no IO of its own. The includer below answers include
names from a map of text; this is the class `CallerIncluderTest` runs on all
three platforms.

```scala
import java.io.File

import org.ekrich.config._

class MapIncluder(entries: Map[String, String])
    extends ConfigIncluder
    with ConfigIncluderFile
    with ConfigIncluderClasspath {

  override def withFallback(fallback: ConfigIncluder): ConfigIncluder = this

  override def include(
      context: ConfigIncludeContext,
      what: String
  ): ConfigObject =
    resolve(context, what)

  override def includeFile(
      context: ConfigIncludeContext,
      what: File
  ): ConfigObject =
    resolve(context, what.getPath())

  override def includeResources(
      context: ConfigIncludeContext,
      what: String
  ): ConfigObject =
    resolve(context, what)

  private def resolve(
      context: ConfigIncludeContext,
      name: String
  ): ConfigObject =
    textFor(name) match {
      case Some(text) =>
        ConfigFactory.parseString(text, context.parseOptions).root
      case None =>
        if (context.parseOptions.getAllowMissing) {
          ConfigFactory.empty.root
        } else {
          throw new ConfigException.IO(
            ConfigFactory.empty.origin,
            "include was not found: '" + name + "'"
          )
        }
    }

  private def textFor(name: String): Option[String] = {
    val candidates =
      if (hasExtension(name)) List(name)
      else List(name + ".conf", name + ".json", name + ".properties")
    candidates.collectFirst {
      case candidate if entries.contains(candidate) => entries(candidate)
    }
  }

  private def hasExtension(name: String): Boolean =
    name.endsWith(".conf") || name.endsWith(".json") ||
      name.endsWith(".properties")
}

val config = ConfigFactory.parseString(
  "include \"base.conf\"\na = 1\n",
  ConfigParseOptions.defaults
    .setIncluder(new MapIncluder(Map("base.conf" -> "b = 2\n")))
)
config.getInt("b") // 2
```

A name reaches the includer exactly as it appears in the `include` statement:
a nested include is looked up as given, not relative to the including file, and
a name without an extension is looked up with each extension in turn. The
includer does not have to answer `url()` includes; implement `ConfigIncluderURL`
if you need them.

## Read from String example

This works on both Scala.js and Scala Native.

```scala
import org.ekrich.config.ConfigFactory

val configStr =
        """
          |maxColumn = 100
          |project.git=true
          |align = none
          |danglingParentheses = true
          |newlines.neverBeforeJsNative = true
          |newlines.sometimesBeforeColonInMethodReturnType = false
          |assumeStandardLibraryStripMargin = true
        """.stripMargin

val conf = ConfigFactory.parseString(configStr)

val maxCol = conf.getInt("maxColumn")
val isGit = conf.getBoolean("project.git")
```

## Using Reader - StringReader example

Both Scala.js and Scala Native support `java.io.Reader` which allows using `sconfig`
to parse all supported formats by passing a filename with extension. See the
following examples from the shared `ConfigFactoryTest` file.

```scala
val filename = "/test01.properties"
val fileStr =
    """
      |# test01.properties file
      |fromProps.abc=abc
      |fromProps.one=1
      |fromProps.bool=true
      |fromProps.specialChars=hello^^
      """.stripMargin

// create Reader
var test01Reader = new StringReader(fileStr)

val config = ConfigFactory.parseReader(
  test01Reader,
  ConfigParseOptions.defaults
    .setSyntaxFromFilename(filename)
)
val specialChars = config.getString("fromProps.specialChars")
```

In Scala Native `java.io.FileReader` is available so you can create a
`FileReader` from a `File`.

### How to read a HOCON configuration file using Scala Native

In order to read a configuration file you need to know the relative
path from where the executable was started or use an absolute path. If the 
Scala Native executable is `run` from `sbt` it will have the current working directory 
equal to the directory at the base of your project where `sbt` was started. If curious
or the situation is unclear you can run the following code inside your Scala Native
application to find the path.

```scala
val dir = System.getProperty("user.dir")
println(s"Working Dir: $dir")
```

Continuing the same thought process you can use the following code to read the file
from a simple `sbt` project where the `src` directory is at the top
level of your project and you are using the `run` command. If you package your
application or run the application executable directly, then making the path relative
to the binary with the code above could be your best option. Another option is to use
the `"user.home"` or the `"user.dir"` property to configure the file path. Note: When the
executable is added to the path, the current working directory is where you start the
executable from so keep that in mind

```scala
import java.io.File
val file = new File("src/main/resources/myapp.conf")
// ConfigDocument
val configDocument = ConfigDocumentFactory.parseFile(file)
// or Config
val config = ConfigFactory.parseFile(file)
```

Using this code with the code above gives you a working solution to use `sconfig`
with Scala Native.

<kbd>[<- Back to README](../README.md)</kbd>
