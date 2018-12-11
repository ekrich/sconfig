// to release, bump major/minor/micro as appropriate,
// update NEWS, update version in README.md, tag, then
// publishSigned.
// Release tags should follow: http://semver.org/

addCommandAlias(
  "run-examples",
  Seq(
    "config-simple-app-scala/run",
    "config-complex-app-scala/run",
    "config-simple-app-java/run",
    "config-complex-app-java/run"
  ).mkString(";", ";", "")
)

val configVersion = "0.7.0-SNAPSHOT"
val scalacOpts    = List("-unchecked", "-deprecation", "-feature")

ThisBuild / version := configVersion
ThisBuild / Compile / scalacOptions := scalacOpts
ThisBuild / Test / scalacOptions := scalacOpts

ThisBuild / crossScalaVersions := Seq("2.12.8", "2.11.12")

inThisBuild(
  List(
    description := "Configuration library for Scala using HOCON files",
    organization := "org.ekrich", // causes package error not being com.typesafe
    homepage := Some(url("https://github.com/ekrich/sconfig")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "ekrich",
        name = "Eric K Richardson",
        email = "ekrichardson@gmail.com",
        url = url("http://github.ekrich.org/")
      )
    )
  ))

ThisBuild / pomIncludeRepository := { _ =>
  false
}

lazy val root = (project in file("."))
  .aggregate(
    testLib,
    configLib,
    simpleLibScala,
    simpleAppScala,
    complexAppScala,
    simpleLibJava,
    simpleAppJava,
    complexAppJava
  )
  .settings(commonSettings)
  .settings(
    name := "config-root",
    doc / aggregate := false,
    doc := (configLib / Compile / doc).value,
    packageDoc / aggregate := false,
    packageDoc := (configLib / Compile / packageDoc).value,
  )

lazy val configLib = Project("config", file("config"))
  .enablePlugins(SbtOsgi)
  .dependsOn(testLib % "test->test")
  .settings(osgiSettings)
  .settings(
    autoScalaLibrary := true,
    crossPaths := false,
    libraryDependencies += {
      val liftVersion = scalaBinaryVersion.value match {
        case "2.10" => "2.6.3" // last version that supports 2.10
        case _      => "3.3.0" // latest version for 2.11 and 2.12
      }
      "net.liftweb" %% "lift-json" % liftVersion % Test
    },
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    Compile / compile / javacOptions ++= Seq("-source",
                                             "1.8",
                                             "-target",
                                             "1.8",
                                             "-g",
                                             "-Xlint:unchecked"),
    Compile / doc / javacOptions ++= Seq(
      "-group",
      s"Public API (version ${version.value})",
      "com.typesafe.config:com.typesafe.config.parser",
      "-group",
      "Internal Implementation - Not ABI Stable",
      "com.typesafe.config.impl"
    ),
    // because we test some global state such as singleton caches,
    // we have to run tests in serial.
    Test / parallelExecution := false,
    test / fork := true,
    Test / fork := true,
    run / fork := true,
    Test / run / fork := true,
    //env vars for tests
    Test / envVars ++= Map("testList.0" -> "0", "testList.1" -> "1"),
    OsgiKeys.exportPackage := Seq("com.typesafe.config",
                                  "com.typesafe.config.impl"),
    Compile / packageBin / packageOptions +=
      Package.ManifestAttributes("Automatic-Module-Name" -> "typesafe.config"),
    // replace with your old artifact id
    mimaPreviousArtifacts := Set("com.typesafe" % "config" % "1.3.3"),
    mimaBinaryIssueFilters ++= ignoredABIProblems
  )

lazy val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._
  Seq(
    exclude[Problem]("com.typesafe.config.impl.*")
  )
}

lazy val commonSettings: Seq[Setting[_]] = Def.settings(
  unpublished
)

def proj(id: String, base: File) = Project(id, base) settings commonSettings

lazy val testLib = proj("config-test-lib", file("test-lib"))

lazy val simpleLibScala = proj(
  "config-simple-lib-scala",
  file("examples/scala/simple-lib")) dependsOn configLib
lazy val simpleAppScala = proj(
  "config-simple-app-scala",
  file("examples/scala/simple-app")) dependsOn simpleLibScala
lazy val complexAppScala = proj(
  "config-complex-app-scala",
  file("examples/scala/complex-app")) dependsOn simpleLibScala

lazy val simpleLibJava = proj(
  "config-simple-lib-java",
  file("examples/java/simple-lib")) dependsOn configLib
lazy val simpleAppJava = proj(
  "config-simple-app-java",
  file("examples/java/simple-app")) dependsOn simpleLibJava
lazy val complexAppJava = proj(
  "config-complex-app-java",
  file("examples/java/complex-app")) dependsOn simpleLibJava

val unpublished = Seq(
  // no artifacts in this project
  publishArtifact := false,
  // make-pom has a more specific publishArtifact setting already
  // so needs specific override
  makePom / publishArtifact := false,
  // no docs to publish
  packageDoc / publishArtifact := false,
  publish / skip := true
)
