# sconfig
![CI](https://github.com/ekrich/sconfig/workflows/CI/badge.svg)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)
[![Scala Native](https://img.shields.io/badge/Scala_Native-0.5.x-blue.svg)](https://scala-native.org)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Configuration library written in [Scala](https://www.scala-lang.org/) which is a direct translation of the original widely used Java library.

Care has been taken to keep the API the same, but some changes were needed to follow Scala conventions. For example, the empty parentheses were dropped from non-side-effecting methods, so `config.root()` is now `config.root`. The [scalafix rule](#migrating-from-lightbendconfig-to-sconfig) handles these changes for you. Using the library from Java is still possible, as the working Java examples show.

[Scala JVM](https://www.scala-lang.org/), [Scala Native](https://scala-native.readthedocs.io/), and [Scala.js](https://www.scala-js.org/)
are supported. Scala JVM is fully supported whereas the other platforms support a subset of the full API.

For motivation and background about this project see the [PR](https://github.com/lightbend/config/pull/600) to the original project. The TLDR is the library was ported to Scala to support Scala Native so [scalafmt](https://scalameta.org/scalafmt/) which uses HOCON configuration could be compiled into a native application.

For the original Java library, see [lightbend/config](https://github.com/lightbend/config).

## Getting Started
[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/org.ekrich/sconfig_3/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.ekrich/sconfig_3/)

To use in `sbt` 2, add the following, replacing `x.y.z` with the version from the Maven Central badge above. All available versions can be seen at the [Maven Repository](https://mvnrepository.com/artifact/org.ekrich/sconfig).

```scala
libraryDependencies += "org.ekrich" %% "sconfig" % "x.y.z"
```

Note: For Scala.js and Scala Native with sbt 1, use `%%%`.

Please refer to the [Usage and Help](#usage-and-help) section below for **critical** Scala Native and Scala.js usage information. The TLDR is that you must add a `java.time` library dependency to your project. Refer to the [`sjavatime` home page](https://github.com/ekrich/sjavatime) for the current version or alternative `java.time` libraries.

If migrating from the original `config` library, see the [migrating section](#migrating-from-lightbendconfig-to-sconfig) below.

## Contributing

See [Contributing](docs/CONTRIBUTING.md) for helpful hints for those wishing to contribute to this repository.

## Cross Build Versions
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)
[![Scala Native](https://img.shields.io/badge/Scala_Native-0.5.x-blue.svg)](https://scala-native.org)

| Scala Version          | JVM | Scala.js (1.x)        | Native (0.5.x) |
| ---------------------- | :-: | :-------------------: | :------------: |
| 2.13.x                 | ✅  |          ✅           |       ✅       |
| 3.x.x                  | ✅  |          ✅           |       ✅       |

* Scala Native 0.5.x support from 0.7.0.
* Scala 3 support on Scala Native 0.4.3-RC2 or greater.
* Scala 2.11 support through version 1.4.9.
* Scala 3.8+ and Scala.js 1.21+ usage requires Java 17 as of v2.0.0+ of sconfig.
* Scala 3.9.0 or greater required as of v2.1.0.
* Scala 2.12 dropped in version 2.2.0.

## Usage and Help
[![scaladoc](https://javadoc.io/badge/org.ekrich/sconfig_3.svg?label=scaladoc3)](https://javadoc.io/doc/org.ekrich/sconfig_3)
[![Discord](https://img.shields.io/discord/633356833498595365.svg?label=&logo=discord&logoColor=ffffff&color=404244&labelColor=6A7EC2)](https://discord.gg/XSj6hQs)

The intent is to keep this library in sync with the original, but each PR needs to be ported to maintain feature parity. Because of this, the *Core Reference* documentation below could contain features that are not yet implemented. Also, we added some *New Features* that **diverge** from the original codebase.

**Core Reference:** Please refer to the original library documentation in the [README](https://github.com/lightbend/config#overview).

**New Features:** Refer to the [New Features](docs/NEW_FEATURES.md) document for changes that are specific to this library.

**Platform Support:** Use this link for a guide to using [Scala Native and Scala.js](docs/SCALA_NATIVE.md).

For specific changes, refer to the [Releases](#releases) below.

## Migrating from [lightbend/config](https://github.com/lightbend/config) to sconfig

[![sconfig Scala version support](https://index.scala-lang.org/ekrich/sconfig/sconfig/latest.svg)](https://index.scala-lang.org/ekrich/sconfig/sconfig)
[![Latest scalafix version](https://index.scala-lang.org/scalacenter/scalafix/scalafix-core/latest.svg)](https://index.scala-lang.org/scalacenter/scalafix/scalafix-core)

This project publishes a [scalafix](https://scalacenter.github.io/scalafix/) rule to migrate existing Scala 2 source code that uses `com.typesafe.config.Config` to this implementation. Scalafix rules modify in place existing valid Scala code. Think of it as a fancy find-and-replace tool that is aware of the Scala type system and can therefore narrowly tailor the changes being made. (Since scalafix changes the source code on your file system, it's best to commit any changes prior to running the rule, in case something weird happens.)

The rule will replace `com.typesafe.config` package references with `org.ekrich.config`, and remove trailing parens on some methods (where the API changed from the Java implementation).

Complete setup documentation and the current `scalafix` version can be found in the [scalafix user guide](https://scalacenter.github.io/scalafix/docs/users/installation.html). At a high level, the process is as follows:

1. Add scalafix to the project's `project/plugins.sbt` file using the version found above:

   ```scala
   addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "a.b.c")
   ```

2. Add this project to the project's `libraryDependencies`, but don't remove the old one yet!
   
   (The old dependency needs to stay on the classpath until after the rule runs, because the code must compile before it will run.)

3. Run the scalafix sbt command shown below to apply the rule using the version of `sconfig` selected. Replace the `x.y.z` below with the version (must be greater than version `1.4.5` when scalafix was added):

    ```
    scalafixEnable; scalafixAll dependency:ReplaceTypesafeConfig@org.ekrich:sconfig-scalafix:x.y.z
    ```

4. Remove the old config dependency from the project's `libraryDependencies`
5. Commit the changes

If you'd rather not use scalafix, especially if you don't use config much, change the dependency, then update the imports and remove the trailing parentheses on non-side-effecting methods. The compiler will point out what's left.

## Releases

Latest Release:  [![Sonatype Central](https://maven-badges.sml.io/sonatype-central/org.ekrich/sconfig_3/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.ekrich/sconfig_3/)

See [RELEASES](docs/RELEASES.md) for details or older releases.
