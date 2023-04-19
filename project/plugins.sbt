// To test snapshots
resolvers ++= Resolver.sonatypeOssRepos("snapshots")

// versions
val crossVer = "1.3.1"
val scalaJSVersion = "1.13.1"
val scalaNativeVersion = "0.4.12"
val scalafix = "0.10.4"

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.11")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.2")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % scalafix)
