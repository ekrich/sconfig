# sconfig
[![Build Status](https://travis-ci.org/ekrich/sconfig.svg?branch=master)](https://travis-ci.org/ekrich/sconfig)

Configuration library for [Scala](https://www.scala-lang.org/) which is a direct translation 
from the original widely used Java library. 

Care has been taken to keep the API the same but source changes are needed to use this library. 
Otherwise we recommend the original [lightbend/config](https://github.com/lightbend/config).


This is a work in progress with the goal to support JVM,
[Scala Native](https://scala-native.readthedocs.io/), and [Scala.js](https://www.scala-js.org/).

## Sonatype
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.ekrich/sconfig/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.ekrich/sconfig)

```scala
libraryDependencies += "org.ekrich" %% "sconfig" % "X.Y.Z"
```

To use in sbt, replace `X.Y.Z` with the version from Maven Central badge above.

## Usage and Help
[![Scaladoc](https://www.javadoc.io/badge/org.ekrich/sconfig_2.12.svg?label=scaladoc)](https://www.javadoc.io/doc/org.ekrich/sconfig_2.12)
[![Join chat https://gitter.im/ekrich/sconfig](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ekrich/sconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The following changes are needed if migrating from the original Java version.
- Change the package organization name from `com.typesafe` to `org.ekrich`.
- Other code sites may require removing `()` where the method just returns state. This is to 
follow the Scala convention which supports the *uniform access principle* [1].

The following document copies are local to this repository but internal links may refer to the
original site documents via hyperlink.

[Overview](docs/original/README.md)

[HOCON Specification](docs/original/HOCON.md)

## Versions

## References
[1]: Odersky, Spoon, & Venners (2016). *Programming in Scala (3rd Ed)*, (pp. 185-188)
