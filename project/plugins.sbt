// versions
val crossVer           = "1.0.0"
val scalaJSVersion     = "1.2.0"
val scalaNativeVersion = "0.4.0-M2"

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.geirsson" % "sbt-ci-release"  % "1.5.3")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.0")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % scalaJSVersion)

// Dotty - Scala 3
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.4.2")
