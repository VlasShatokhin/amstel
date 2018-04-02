import sbt.Keys._
import sbt.{Project, _}
import sbtassembly.AssemblyPlugin
import sbtassembly.AssemblyKeys._

object ProjectProfiles {

  type Profile = Project => Project

  object DependenciesVersions {
    val akkaVersion = "2.5.11"
    val akkaHttpVersion = "10.1.0"
    val scalaTestVersion = "3.0.4"
    val macwireVersion = "2.3.1"
  }

  import DependenciesVersions._

  val commonBuildSettings = Seq(
    organization := "io.vlas.amstel",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-Xexperimental",
      "-Xlint",
      "-feature",
      "-target:jvm-1.8",
      "-Xfatal-warnings"),
    crossPaths := false,
    sourcesInBase := false,
    target := (baseDirectory in ThisBuild).value / "target" / thisProject.value.id)

  def rootProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .settings(
      EventLoad.numDevices := 3,
      EventLoad.host := "localhost",
      EventLoad.port := 8080,
      EventLoad.generateLoad := EventLoad.startLoadGeneration.value)
    .disablePlugins(AssemblyPlugin)

  def coreProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream"  % akkaVersion)
    )
    .disablePlugins(AssemblyPlugin)

  def apiProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided,
        "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % Provided,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
      ),
      mainClass in assembly := Some("io.vlas.amstel.api.StatisticsApp")
    )

  def stateProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "org.scalatest" %% "scalatest" % scalaTestVersion % Test)
    )
    .disablePlugins(AssemblyPlugin)

  def publisherProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .disablePlugins(AssemblyPlugin)

  def statisticsReaderProfile: Profile = _
    .settings(commonBuildSettings: _*)
    .disablePlugins(AssemblyPlugin)

}
