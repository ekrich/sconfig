addCommandAlias(
  "run-examples",
  Seq(
    "sconfig-simple-app-scala/run",
    "sconfig-complex-app-scala/run",
    "sconfig-simple-app-java/run",
    "sconfig-complex-app-java/run"
  ).mkString(";", ";", "")
)

val prevVersion = "1.3.3"
val nextVersion = "1.3.4"

// stable snapshot is not great for publish local
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val tag = out.ref.dropV.value
  if (out.isCleanAfterTag) tag
  else nextVersion + "-SNAPSHOT"
}

val scalacOpts = List(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused:imports",
  "-Xsource:3",
  "-Xlint:nonlocal-return"
)

val dotcOpts = List("-Xdiags:verbose")

ThisBuild / Compile / scalacOptions := {
  if (isDotty.value) dotcOpts else scalacOpts
}
ThisBuild / Test / scalacOptions := {
  if (isDotty.value) dotcOpts else scalacOpts
}

scalacOptions in (Compile, console) --= Seq(
  "-Ywarn-unused:imports",
  "-Xfatal-warnings"
)

val scala211 = "2.11.12"
val scala212 = "2.12.12"
val scala213 = "2.13.3"
val dotty    = "0.27.0-RC1"

val versionsBase   = Seq(scala211, scala212, scala213, dotty)
val versionsJVM    = versionsBase
val versionsJS     = versionsBase
val versionsNative = Seq(scala211)

ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := versionsJVM

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

ThisBuild / pomIncludeRepository := { _ => false }

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
    scala2or3Source,
    libraryDependencies += ("org.scala-lang.modules" %%% "scala-collection-compat" % "2.2.0")
      .withDottyCompat(scalaVersion.value)
  )
  .jvmSettings(
    sharedJvmNativeSource,
    libraryDependencies ++= Seq(
      "io.crashbox"  %% "spray-json"     % "1.3.5-7" % Test,
      "com.novocode" % "junit-interface" % "0.11"    % Test
    ).map(_.withDottyCompat(scalaVersion.value)),
    Compile / compile / javacOptions ++= Seq(
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-g",
      "-Xlint:unchecked"
    ),
    // Dotty is missing serializable support
    // Can Filter based on Test name but not method name with "erializ"
    // so exclude the Tests with the 19 that cannot pass
    // 530 - 19 = 511 Only 346 get run this way so we lose coverage
    Test / testOptions := {
      if (isDotty.value)
        Seq(
          Tests.Exclude(
            Seq(
              "org.ekrich.config.impl.ValidationTest",
              "org.ekrich.config.impl.PublicApiTest",
              "org.ekrich.config.impl.ConfigValueTest",
              "org.ekrich.config.impl.ConfigTest"
            )
          )
        )
      else Seq(Tests.Exclude(Seq()))
    },
    // because we test some global state such as singleton caches,
    // we have to run tests in serial.
    Test / parallelExecution := false,
    test / fork := true,
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
    scalaVersion := scala211, // allows to compile if scalaVersion set not 2.11
    sharedJvmNativeSource,
    nativeLinkStubs := true,
    logLevel := Level.Info, // Info or Debug
    libraryDependencies += "com.github.lolgab" %%% "minitest" % "2.5.0-5f3852e" % Test,
    testFrameworks += new TestFramework("minitest.runner.Framework")
  )
  .jsSettings(
    crossScalaVersions := versionsJS,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-java-time"         % "1.0.0",
      "org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion % "test"
    ).map(_.withDottyCompat(scalaVersion.value))
  )

lazy val sharedJvmNativeSource: Seq[Setting[_]] = Def.settings(
  Compile / unmanagedSourceDirectories +=
    (ThisBuild / baseDirectory).value
      / "sconfig" / "sharedjvmnative" / "src" / "main" / "scala"
)

lazy val scala2or3Source: Seq[Setting[_]] = Def.settings(
  Compile / unmanagedSourceDirectories +=
    (ThisBuild / baseDirectory).value
      / "sconfig" / { if (isDotty.value) "sharedScala3" else "sharedScala2" }
      / "src" / "main" / "scala"
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
