// To test snapshots
resolvers += Resolver.sonatypeCentralSnapshots

// versions
val crossVer = "1.3.2"
val scalaJSVersion = "1.20.1"
val scalaNativeVersion = "0.5.9"
val scalafix = "0.14.5"

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % scalafix)

// Run scalafmt within sbt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
