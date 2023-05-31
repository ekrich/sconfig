addCommandAlias(
  "run-examples",
  Seq(
    "sconfig-simple-app-scala/run",
    "sconfig-complex-app-scala/run",
    "sconfig-simple-app-java/run",
    "sconfig-complex-app-java/run"
  ).mkString(";", ";", "")
)

val prevVersion = "1.5.0"
val nextVersion = "1.5.1"

// stable snapshot is not great for publish local
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val tag = out.ref.dropPrefix
  if (out.isCleanAfterTag) tag
  else nextVersion + "-SNAPSHOT"
}

val dotcOpts = List("-unchecked", "-deprecation", "-feature")
val scalacOpts = dotcOpts ++ List(
  "-Ywarn-unused:imports",
  "-Xsource:3"
)

Compile / console / scalacOptions --= Seq(
  // "-Xlint:nonlocal-return", // for 2.12 console
  "-Ywarn-unused:imports",
  "-Xfatal-warnings"
)

val isScala3 = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => true
    case _            => false
  }
}

val scala212 = "2.12.17"
val scala213 = "2.13.10"
val scala3 = "3.3.0"

val javaTime = "1.1.9"
val scCompat = "2.10.0"

val versionsBase = Seq(scala212, scala213)
val versions = versionsBase :+ scala3

ThisBuild / scalaVersion := scala3
ThisBuild / crossScalaVersions := versions
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / mimaFailOnNoPrevious := false
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

Compile / packageBin / packageOptions +=
  Package.ManifestAttributes("Automatic-Module-Name" -> "org.ekrich.sconfig")

inThisBuild(
  List(
    version := dynverGitDescribeOutput.value.mkVersion(versionFmt, ""),
    dynver := sbtdynver.DynVer
      .getGitDescribeOutput(new java.util.Date)
      .mkVersion(versionFmt, ""),
    description := "Configuration library for Scala using HOCON files",
    organization := "org.ekrich",
    homepage := Some(url("https://github.com/ekrich/sconfig")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        id = "ekrich",
        name = "Eric K Richardson",
        email = "ekrichardson@gmail.com",
        url = url("http://github.ekrich.org/")
      )
    )
  )
)

lazy val root = (project in file("."))
  .aggregate(
    testLibJVM,
    sconfigJVM,
    sconfigNative,
    sconfigJS,
    `scalafix-rules`,
    `scalafix-tests`,
    simpleLibScala,
    simpleAppScala,
    complexAppScala,
    simpleLibJava,
    simpleAppJava,
    complexAppJava
  )
  .settings(commonSettings)
  .settings(
    name := "sconfig-root",
    crossScalaVersions := Nil,
    doc / aggregate := false,
    doc := (sconfigJVM / Compile / doc).value,
    packageDoc / aggregate := false,
    packageDoc := (sconfigJVM / Compile / packageDoc).value
  )

lazy val sconfig = crossProject(JVMPlatform, NativePlatform, JSPlatform)
  .crossType(CrossType.Full)
  .settings(
    scalacOptions ++= {
      if (isScala3.value) dotcOpts else scalacOpts
    },
    libraryDependencies += "org.scala-lang.modules" %%% "scala-collection-compat" % scCompat,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
  )
  .jvmSettings(
    crossScalaVersions := versions,
    libraryDependencies ++= Seq(
      ("io.crashbox" %% "spray-json" % "1.3.5-7" % Test)
        .cross(CrossVersion.for3Use2_13),
      "com.github.sbt" % "junit-interface" % "0.13.3" % Test
      // includes junit 4.13.2
    ),
    Compile / compile / javacOptions ++= Seq(
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-g",
      "-Xlint:unchecked"
    ),
    // because we test some global state such as singleton caches,
    // we have to run tests in serial.
    Test / parallelExecution := false,
    Test / fork := true,
    run / fork := true,
    Test / run / fork := true,
    // env vars for tests
    Test / envVars ++= Map(
      "testList.0" -> "0",
      "testList.1" -> "1",
      "testClassesPath" -> (Test / classDirectory).value.getPath
    ),
    // uncomment for debugging
    // Test / javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
    // mima settings
    mimaPreviousArtifacts := Set("org.ekrich" %% "sconfig" % prevVersion),
    mimaBinaryIssueFilters ++= ignoredABIProblems
  )
  .nativeConfigure(_.enablePlugins(ScalaNativeJUnitPlugin))
  .nativeSettings(
    crossScalaVersions := versions,
    nativeConfig ~= (_.withLinkStubs(true)),
    logLevel := Level.Info, // Info or Debug
    libraryDependencies += "org.ekrich" %%% "sjavatime" % javaTime % "provided"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    crossScalaVersions := versions,
    libraryDependencies += "org.ekrich" %%% "sjavatime" % javaTime % "provided"
  )

lazy val `scalafix-rules` = (project in file("scalafix/rules"))
  .settings(
    moduleName := "sconfig-scalafix",
    crossScalaVersions := versionsBase,
    libraryDependencies ++= Seq(
      "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion
    )
  )

lazy val `scalafix-input` = (project in file("scalafix/input"))
  .settings(
    crossScalaVersions := versionsBase,
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3"
    ),
    scalacOptions ~= { _.filterNot(_ == "-Xfatal-warnings") },
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )

lazy val `scalafix-output` = (project in file("scalafix/output"))
  .settings(
    crossScalaVersions := versionsBase,
    publish / skip := true,
    scalacOptions ~= { _.filterNot(_ == "-Xfatal-warnings") }
  )
  .dependsOn(sconfigJVM)

lazy val `scalafix-tests` = (project in file("scalafix/tests"))
  .settings(
    crossScalaVersions := versionsBase,
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % _root_.scalafix.sbt.BuildInfo.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := (`scalafix-output` / Compile / unmanagedSourceDirectories).value,
    scalafixTestkitInputSourceDirectories := (`scalafix-input` / Compile / unmanagedSourceDirectories).value,
    scalafixTestkitInputClasspath := (`scalafix-input` / Compile / fullClasspath).value,
    scalafixTestkitInputScalacOptions := (`scalafix-input` / Compile / scalacOptions).value,
    scalafixTestkitInputScalaVersion := (`scalafix-input` / Compile / scalaVersion).value
  )
  .dependsOn(`scalafix-rules`)
  .enablePlugins(ScalafixTestkitPlugin)

lazy val sconfigJVM = sconfig.jvm
  .dependsOn(testLibJVM % "test->test")
lazy val sconfigNative = sconfig.native
lazy val sconfigJS = sconfig.js

lazy val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._
  Seq(
    exclude[Problem]("org.ekrich.config.impl.*"),
    exclude[Problem]("scala.collection.compat.*"),
    exclude[Problem]("scala.jdk.CollectionConverters*")
  )
}

lazy val commonSettings: Seq[Setting[_]] =
  Def.settings(
    skipPublish
  )

def proj(id: String, base: File) =
  Project(id, base) settings commonSettings

lazy val testLibJVM = testLib.jvm

lazy val testLib = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("test-lib"))
  .settings(
    name := "sconfig-test-lib",
    publish / skip := true
  )

lazy val simpleLibScala = proj(
  "sconfig-simple-lib-scala",
  file("examples/scala/simple-lib")
) dependsOn sconfigJVM
lazy val simpleAppScala = proj(
  "sconfig-simple-app-scala",
  file("examples/scala/simple-app")
) dependsOn simpleLibScala
lazy val complexAppScala = proj(
  "sconfig-complex-app-scala",
  file("examples/scala/complex-app")
) dependsOn simpleLibScala

lazy val simpleLibJava = proj(
  "sconfig-simple-lib-java",
  file("examples/java/simple-lib")
) dependsOn sconfigJVM
lazy val simpleAppJava = proj(
  "sconfig-simple-app-java",
  file("examples/java/simple-app")
) dependsOn simpleLibJava
lazy val complexAppJava = proj(
  "sconfig-complex-app-java",
  file("examples/java/complex-app")
) dependsOn simpleLibJava

val skipPublish = Seq(
  // no artifacts in this project
  publishArtifact := false,
  // make-pom has a more specific publishArtifact setting already
  // so needs specific override
  makePom / publishArtifact := false,
  // no docs to publish
  packageDoc / publishArtifact := false,
  publish / skip := true
)
