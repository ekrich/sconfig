// versions
val scalaJSVersion     = "0.6.28"
val scalaNativeVersion = "0.3.9"
val crossVer           = "0.6.0"

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.geirsson" % "sbt-ci-release"  % "1.2.6")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.3.0")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
