# sconfig
![CI](https://github.com/ekrich/sconfig/workflows/CI/badge.svg)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Configuration library written in [Scala](https://www.scala-lang.org/) which is a direct translation of the original widely used Java library.

[Scala JVM](https://www.scala-lang.org/), [Scala Native](https://scala-native.readthedocs.io/), and [Scala.js](https://www.scala-js.org/)
are supported. Scala JVM is fully supported whereas the other platforms support a subset of the full API.

For motivation and background about this project see the [PR](https://github.com/lightbend/config/pull/600) to the original project. The TLDR is the library was ported to Scala to support Scala Native so [scalafmt](https://scalameta.org/scalafmt/) which uses HOCON configuration could be compiled into a native application.

Care has been taken to keep the API the same but changes were needed when moving from the Java API. Using Java is also possible as demonstrated by including the working Java examples.

If you are looking for the original Java API, see
[https://github.com/lightbend/config](https://github.com/lightbend/config).


## Getting Started
[![Maven Central](https://img.shields.io/maven-central/v/org.ekrich/sconfig_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/org.ekrich/sconfig_2.13)

```scala
libraryDependencies += "org.ekrich" %% "sconfig" % "x.y.z"
```

To use in `sbt`, replace `x.y.z` with the version from Maven Central badge above.

For non-JVM projects use `%%%` but please refer to the guide below for **critical** Scala Native and Scala.js usage information. The TLDR is that you must add a `java.time` library dependency to your project. Refer to the [`sjavatime` home page](https://github.com/ekrich/sjavatime) for the current version or alternative `java.time` libraries.

[Scala Native and Scala.js](docs/SCALA-NATIVE.md) - A guide to using Scala Native and Scala.js.

All available versions can be seen at the [Maven Repository](https://mvnrepository.com/artifact/org.ekrich/sconfig).

## Cross Build Versions
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)

| Scala Version          | JVM | Scala.js (1.x)        | Native (0.5.x) |
| ---------------------- | :-: | :-------------------: | :------------: |
| 2.12.x                 | ✅  |          ✅           |       ✅       |
| 2.13.x                 | ✅  |          ✅           |       ✅       |
| 3.x.x                  | ✅  |          ✅           |       ✅       |

* Scala Native 0.5.x support from 0.7.0.
* Scala 3 support on Scala Native 0.4.3-RC2 or greater.
* Scala 2.11 support through version 1.4.9.

## Usage and Help
[![scaladoc](https://javadoc.io/badge/org.ekrich/sconfig_3.svg?label=scaladoc3)](https://javadoc.io/doc/org.ekrich/sconfig_3)
[![Discord](https://img.shields.io/discord/633356833498595365.svg?label=&logo=discord&logoColor=ffffff&color=404244&labelColor=6A7EC2)](https://discord.gg/XSj6hQs)


Please refer to the original library documentation [here](https://github.com/lightbend/config). This is to reduce the maintenance burden of this library.

The intent is to keep the library in sync with the original but each PR needs to be be ported to maintain feature parity. The documentation could contain features that are not yet implemented in this library.

For specific changes, refer to the releases below.

## Migrating an existing [lightbend/config](https://github.com/lightbend/config) project to sconfig

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
    scalafixEnable; scalafixAll dependency:ReplaceTypesafeConfig@org.ekrich:sconfig-scalafix:x.y.x
    ```

4. Remove the old config dependency from the project's `libraryDependencies`
5. Commit the changes

## Versions

Release [1.7.0](https://github.com/ekrich/sconfig/releases/tag/v1.7.0) - (2023-04-16)<br/>
Release [1.6.0](https://github.com/ekrich/sconfig/releases/tag/v1.6.0) - (2023-12-28)<br/>
Release [1.5.1](https://github.com/ekrich/sconfig/releases/tag/v1.5.1) - (2023-09-15)<br/>
Release [1.5.0](https://github.com/ekrich/sconfig/releases/tag/v1.5.0) - (2022-09-19)<br/>
Release [1.4.9](https://github.com/ekrich/sconfig/releases/tag/v1.4.9) - (2022-01-25)<br/>
Release [1.4.8](https://github.com/ekrich/sconfig/releases/tag/v1.4.8) - (2022-01-12)<br/>
Release [1.4.7](https://github.com/ekrich/sconfig/releases/tag/v1.4.7) - (2022-01-03)<br/>
Release [1.4.6](https://github.com/ekrich/sconfig/releases/tag/v1.4.6) - (2021-12-06)<br/>
Release [1.4.5](https://github.com/ekrich/sconfig/releases/tag/v1.4.5) - (2021-10-08)<br/>
Release [1.4.4](https://github.com/ekrich/sconfig/releases/tag/v1.4.4) - (2021-05-13)<br/>
Release [1.4.3](https://github.com/ekrich/sconfig/releases/tag/v1.4.3) - (2021-05-12)<br/>
Release [1.4.2](https://github.com/ekrich/sconfig/releases/tag/v1.4.2) - (2021-04-01)<br/>
Release [1.4.1](https://github.com/ekrich/sconfig/releases/tag/v1.4.1) - (2021-02-24)<br/>
Release [1.4.0](https://github.com/ekrich/sconfig/releases/tag/v1.4.0) - (2021-01-26)<br/>
Release [1.3.6](https://github.com/ekrich/sconfig/releases/tag/v1.3.6) - (2020-12-21)<br/>
Release [1.3.5](https://github.com/ekrich/sconfig/releases/tag/v1.3.5) - (2020-11-24)<br/>
Release [1.3.4](https://github.com/ekrich/sconfig/releases/tag/v1.3.4) - (2020-11-03)<br/>
Release [1.3.3](https://github.com/ekrich/sconfig/releases/tag/v1.3.3) - (2020-09-14)<br/>
Release [1.3.2](https://github.com/ekrich/sconfig/releases/tag/v1.3.2) - (2020-09-01)<br/>
Release [1.3.1](https://github.com/ekrich/sconfig/releases/tag/v1.3.1) - (2020-07-24)<br/>
Release [1.3.0](https://github.com/ekrich/sconfig/releases/tag/v1.3.0) - (2020-05-01)<br/>
Release [1.2.2](https://github.com/ekrich/sconfig/releases/tag/v1.2.2) - (2020-04-28)<br/>
Release [1.2.1](https://github.com/ekrich/sconfig/releases/tag/v1.2.1) - (2020-02-18)<br/>
Release [1.1.3](https://github.com/ekrich/sconfig/releases/tag/v1.1.3) - (2019-12-27)<br/>
Release [1.0.0](https://github.com/ekrich/sconfig/releases/tag/v1.0.0) - (2019-08-05)<br/>
Release [0.9.2](https://github.com/ekrich/sconfig/releases/tag/v0.9.2) - (2019-06-10)<br/>
Release [0.9.1](https://github.com/ekrich/sconfig/releases/tag/v0.9.1) - (2019-05-22)<br/>
Release [0.9.0](https://github.com/ekrich/sconfig/releases/tag/v0.9.0) - (2019-05-21)<br/>
Release [0.8.0](https://github.com/ekrich/sconfig/releases/tag/v0.8.0) - (2019-04-23)<br/>
Release [0.7.6](https://github.com/ekrich/sconfig/releases/tag/v0.7.6) - (2019-04-10)<br/>
Release [0.7.5](https://github.com/ekrich/sconfig/releases/tag/v0.7.5) - (2019-04-05)<br/>
Release [0.7.0](https://github.com/ekrich/sconfig/releases/tag/v0.7.0) - (2018-12-14)
