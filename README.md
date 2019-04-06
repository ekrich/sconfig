# sconfig
[![Build Status](https://travis-ci.org/ekrich/sconfig.svg?branch=master)](https://travis-ci.org/ekrich/sconfig)

Configuration library written in [Scala](https://www.scala-lang.org/) which is a direct translation 
of the original widely used Java library.

Scala on the JVM is currently supported with the goal to add support for
[Scala Native](https://scala-native.readthedocs.io/), and [Scala.js](https://www.scala-js.org/).

For motivation and background about this project see the [PR](https://github.com/lightbend/config/pull/600) 
to the original project.

Care has been taken to keep the API the same but changes are needed when moving from the Java API.
Using Java is also possible because the included Java examples work.

If you are looking for the original proven Java API, see
[https://github.com/lightbend/config](https://github.com/lightbend/config).


## Sonatype
[![Maven Central](https://img.shields.io/maven-central/v/org.ekrich/sconfig_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/org.ekrich/sconfig_2.11)

```scala
libraryDependencies += "org.ekrich" %% "sconfig" % "X.Y.Z"
```

To use in sbt, replace `X.Y.Z` with the version from Maven Central badge above.

## Usage and Help
[![Scaladoc](https://www.javadoc.io/badge/org.ekrich/sconfig_2.11.svg?label=scaladoc)](https://www.javadoc.io/doc/org.ekrich/sconfig_2.11)
[![Join chat https://gitter.im/ekrich/sconfig](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ekrich/sconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The following document copies are local to this repository but internal links may refer to the
original site documents via hyperlink.

[Overview](docs/original/README.md) - A comprehensive guide to configuration.

[HOCON](docs/original/HOCON.md) - The HOCON Specification

For specific changes, refer to the releases below.

## Versions

Release [0.7.5](https://github.com/ekrich/sconfig/releases/tag/v0.7.5) - (2019-04-05)

Release [0.7.0](https://github.com/ekrich/sconfig/releases/tag/v0.7.0) - (2018-12-14)
