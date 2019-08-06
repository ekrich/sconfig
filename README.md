# sconfig
[![Build Status](https://travis-ci.org/ekrich/sconfig.svg?branch=master)](https://travis-ci.org/ekrich/sconfig)

Configuration library written in [Scala](https://www.scala-lang.org/) which is a direct translation 
of the original widely used Java library.

Scala JVM, [Scala Native](https://scala-native.readthedocs.io/), and [Scala.js](https://www.scala-js.org/)
are supported. Scala JVM is fully supported whereas the other platforms support a subset of the full API.

For motivation and background about this project see the [PR](https://github.com/lightbend/config/pull/600) 
to the original project. The TLDR is the library was ported to Scala to support Scala Native so
[scalafmt](https://scalameta.org/scalafmt/) which uses HOCON configuration could be compiled into
a native application.

Care has been taken to keep the API the same but changes were needed when moving from the Java API.
Using Java is also possible as demonstrated by including the working Java examples.

If you are looking for the original proven Java API, see
[https://github.com/lightbend/config](https://github.com/lightbend/config).


## Sonatype
[![Maven Central](https://img.shields.io/maven-central/v/org.ekrich/sconfig_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/org.ekrich/sconfig_2.11)

```scala
libraryDependencies += "org.ekrich" %%% "sconfig" % "x.y.z"
```

To use in `sbt`, replace `x.y.z` with the version from Maven Central badge above.
All available versions can be seen at the [Maven Repository](https://mvnrepository.com/artifact/org.ekrich/sconfig).

## Usage and Help
[![Scaladoc](https://www.javadoc.io/badge/org.ekrich/sconfig_2.11.svg?label=scaladoc)](https://www.javadoc.io/doc/org.ekrich/sconfig_2.11)
[![Join chat https://gitter.im/ekrich/sconfig](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ekrich/sconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Please refer to the original library documentation [here](https://github.com/lightbend/config).
This is to reduce the maintenance burden of this library.

The intent is to keep the library in sync with the original but each PR needs to be be ported
to maintain feature parity. The documentation could contain features that are not yet implemented
in this library.

[Scala Native and Scala.js](docs/SCALA-NATIVE.md) - A guide to using Scala Native and Scala.js.

For specific changes, refer to the releases below.

## Versions

Release [1.0.0](https://github.com/ekrich/sconfig/releases/tag/v1.0.0) - (2019-08-05)<br/>
Release [0.9.2](https://github.com/ekrich/sconfig/releases/tag/v0.9.2) - (2019-06-10)<br/>
Release [0.9.1](https://github.com/ekrich/sconfig/releases/tag/v0.9.1) - (2019-05-22)<br/>
Release [0.9.0](https://github.com/ekrich/sconfig/releases/tag/v0.9.0) - (2019-05-21)<br/>
Release [0.8.0](https://github.com/ekrich/sconfig/releases/tag/v0.8.0) - (2019-04-23)<br/>
Release [0.7.6](https://github.com/ekrich/sconfig/releases/tag/v0.7.6) - (2019-04-10)<br/>
Release [0.7.5](https://github.com/ekrich/sconfig/releases/tag/v0.7.5) - (2019-04-05)<br/>
Release [0.7.0](https://github.com/ekrich/sconfig/releases/tag/v0.7.0) - (2018-12-14)
