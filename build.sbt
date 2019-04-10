// to release, bump major/minor/micro as appropriate,
// update NEWS, update version in README.md, tag, then
// publishSigned.
// Release tags should follow: http://semver.org/

addCommandAlias(
  "run-examples",
  Seq(
    "sconfig-simple-app-scala/run",
    "sconfig-complex-app-scala/run",
    "sconfig-simple-app-java/run",
    "sconfig-complex-app-java/run"
  ).mkString(";", ";", "")
)

val prevVersion = "0.7.0"
val nextVersion = "0.8.0"

// stable snapshot is not great for publish local
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val tag = out.ref.dropV.value
  if (out.isCleanAfterTag) tag
  else nextVersion + "-SNAPSHOT"
}

val scalacOpts = List("-unchecked", "-deprecation", "-feature")

ThisBuild / Compile / scalacOptions := scalacOpts
ThisBuild / Test / scalacOptions := scalacOpts

val scala211 = "2.11.12"
val scala212 = "2.12.8"
val scala213 = "2.13.0-RC1"
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)

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
    name := "sconfig-root",
    doc / aggregate := false,
    doc := (configLib / Compile / doc).value,
    packageDoc / aggregate := false,
    packageDoc := (configLib / Compile / packageDoc).value,
  )

lazy val configLib = Project("sconfig", file("sconfig"))
  .dependsOn(testLib % "test->test")
  .settings(
    libraryDependencies += "io.crashbox"  %% "spray-json"     % "1.3.5-3" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11"    % Test,
    Compile / compile / javacOptions ++= Seq("-source",
                                             "1.8",
                                             "-target",
                                             "1.8",
                                             "-g",
                                             "-Xlint:unchecked"),
    // because we test some global state such as singleton caches,
    // we have to run tests in serial.
    Test / parallelExecution := false,
    test / fork := true,
    Test / fork := true,
    run / fork := true,
    Test / run / fork := true,
    //env vars for tests
    Test / envVars ++= Map(
      "testList.0"      -> "0",
      "testList.1"      -> "1",
      "testClassesPath" -> (Test / classDirectory).value.getPath),
    // replace with your old artifact id
    mimaPreviousArtifacts := Set("org.ekrich" %% "sconfig" % prevVersion),
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

lazy val testLib = proj("sconfig-test-lib", file("test-lib"))

lazy val simpleLibScala = proj(
  "sconfig-simple-lib-scala",
  file("examples/scala/simple-lib")) dependsOn configLib
lazy val simpleAppScala = proj(
  "sconfig-simple-app-scala",
  file("examples/scala/simple-app")) dependsOn simpleLibScala
lazy val complexAppScala = proj(
  "sconfig-complex-app-scala",
  file("examples/scala/complex-app")) dependsOn simpleLibScala

lazy val simpleLibJava = proj(
  "sconfig-simple-lib-java",
  file("examples/java/simple-lib")) dependsOn configLib
lazy val simpleAppJava = proj(
  "sconfig-simple-app-java",
  file("examples/java/simple-app")) dependsOn simpleLibJava
lazy val complexAppJava = proj(
  "sconfig-complex-app-java",
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
