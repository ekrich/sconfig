// To test snapshots
resolvers ++= Resolver.sonatypeOssRepos("snapshots")

// versions
val crossVer = "1.3.2"
val scalaJSVersion = "1.19.0"
val scalaNativeVersion = "0.5.8"
val scalafix = "0.14.3"

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.1")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % scalafix)
