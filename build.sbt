addCommandAlias(
  "run-examples",
  Seq(
    "sconfig-simple-app-scala/run",
    "sconfig-complex-app-scala/run",
    "sconfig-simple-app-java/run",
    "sconfig-complex-app-java/run"
  ).mkString(";", ";", "")
)

val prevVersion = "1.4.4"
val nextVersion = "1.4.5"

// stable snapshot is not great for publish local
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val tag = out.ref.dropPrefix
  if (out.isCleanAfterTag) tag
  else nextVersion + "-SNAPSHOT"
}

val scalacOpts = List(
  "-unchecked",
  "-deprecation",
  "-feature",
  //"-Ywarn-unused:imports", // no 2.11 - maybe time for sbt-tpolecat
  "-Xsource:3"
  //"-Xlint:nonlocal-return" // no 2.11/2.12
)

val dotcOpts = List("-unchecked", "-deprecation", "-feature")

Compile / console / scalacOptions --= Seq(
  "-Xlint:nonlocal-return", // for 2.12 console
  "-Ywarn-unused:imports",
  "-Xfatal-warnings"
)

val isScala3 = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => true
    case _            => false
  }
}

val scala211 = "2.11.12"
val scala212 = "2.12.13"
val scala213 = "2.13.6"
val scala300 = "3.0.0"

val javaTime = "1.1.5"
val scCompat = "2.4.4"

val versionsBase   = Seq(scala211, scala212, scala213)
val versionsJVM    = versionsBase :+ scala300
val versionsJS     = versionsJVM
val versionsNative = versionsBase

ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := versionsBase
ThisBuild / versionScheme := Some("early-semver")

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
    sharedScala2or3Source,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 =>
          Seq("org.scala-lang.modules" %%% "scala-collection-compat" % scCompat)
        case _ => Nil
      }
    }
  )
  .jvmSettings(
    crossScalaVersions := versionsJVM,
    sharedJvmNativeSource,
    libraryDependencies ++= Seq(
      ("io.crashbox" %% "spray-json" % "1.3.5-7" % Test)
        .cross(CrossVersion.for3Use2_13),
      "com.novocode" % "junit-interface" % "0.11" % Test
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
      "testList.0"      -> "0",
      "testList.1"      -> "1",
      "testClassesPath" -> (Test / classDirectory).value.getPath
    ),
    // uncomment for debugging
    //Test / javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
    // mima settings
    mimaPreviousArtifacts := Set("org.ekrich" %% "sconfig" % prevVersion),
    mimaBinaryIssueFilters ++= ignoredABIProblems
  )
  .nativeSettings(
    crossScalaVersions := versionsNative,
    sharedJvmNativeSource,
    nativeLinkStubs := true,
    logLevel := Level.Info, // Info or Debug
    libraryDependencies += "org.ekrich" %%% "sjavatime" % javaTime % "provided",
    addCompilerPlugin(
      "org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full
    ),
    libraryDependencies += "org.scala-native" %%% "junit-runtime" % nativeVersion,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    crossScalaVersions := versionsJS,
    libraryDependencies += "org.ekrich" %%% "sjavatime" % javaTime % "provided"
  )

lazy val sharedScala2or3Source: Seq[Setting[_]] = Def.settings(
  Compile / unmanagedSourceDirectories ++= {
    val projectDir = baseDirectory.value.getParentFile()
    sourceDir(projectDir, scalaVersion.value)
  }
)

// For Scala 2/3 enums
def sourceDir(projectDir: File, scalaVersion: String): Seq[File] = {
  def versionDir(versionDir: String): File =
    projectDir / "shared" / "src" / "main" / versionDir

  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) => Seq(versionDir("scala-3"))
    case Some((2, _)) => Seq(versionDir("scala-2"))
    case _            => Seq() // unknown version
  }
}

lazy val sharedJvmNativeSource: Seq[Setting[_]] = Def.settings(
  Compile / unmanagedSourceDirectories += {
    val projectDir = baseDirectory.value.getParentFile()
    projectDir / "shared" / "src" / "main" / "scala-jvm-native"
  }
)

lazy val sconfigJVM = sconfig.jvm
  .dependsOn(testLibJVM % "test->test")
lazy val sconfigNative = sconfig.native
lazy val sconfigJS     = sconfig.js

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
