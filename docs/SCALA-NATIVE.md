# Scala Native and Scala.js Help

The release has minimal capabilities at this time so
the following is an example of how to use the API.

## Dependency for Scala Native and Scala.js

Use three percent signs `%%%` instead of two for dependency resolution.

```scala
// sconfig uses "provided" so you have a choice of the java.time API you use
// Refer to your preferred java.time project for version to use
libraryDependencies ++= Seq(
  "org.ekrich" %%% "sconfig" % "x.y.z",
  "org.ekrich" %%% "sjavatime" % "a.b.c"
),
// required to avoid linking errors
nativeLinkStubs := true
```
Please refer to the `sjavatime` [home page](https://github.com/ekrich/sjavatime)
for the current version.

## Read from String example

This assumes that you read a file into a `configStr` first. Other methods
that don't use `File` or `URL` should also work.

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

Both JS and Native now support `java.io.Reader` which allows using `sconfig`
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

### How to read a HOCON configuation file into a String for Scala Native

In order to read the configuration file into a `String` you need to know the relative
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
into a `String` from a simple `sbt` project where the `src` directory is at the top
level of your project and you are using the `run` command. If you package your
application or run the application executable directly, then making the path relative
to the binary with the code above could be your best option. Another option is to use
the `"user.home"` or the `"user.dir"` property to configure the file path.

```scala
import java.nio.file.{Files, Paths}
val bytes = Files.readAllBytes(Paths.get("src/main/resources/myapp.conf"))
val configStr = new String(bytes)
```

Using this code with the code above gives you a working solution to use `sconfig`
with Scala Native.

[Back to README](../README.md)
