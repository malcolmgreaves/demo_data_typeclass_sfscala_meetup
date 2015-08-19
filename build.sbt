organization := "io.malcolmgreaves"

name := "abstract_data"

version := "0.0.1"

// use sbt-dev-settings to configure

import com.nitro.build._

import PublishHelpers._

// scala & java

//                         :::   NOTE   :::
// we want to update to JVM 8 ASAP !
// since we know that we want to be able to use this stuff w/ Spark,
// we unfortunately have to limit ourselves to jvm 7.
// once this gets resolved, we'll update: 
// [JIRA Issue]     https://issues.apache.org/jira/browse/SPARK-6152

lazy val devConfig = {
  import CompileScalaJava._
  Config.spark.copy(scala = Config.spark.scala.copy(isScala211 = true))
}

CompileScalaJava.librarySettings(devConfig)

javaOptions := JvmRuntime.settings(devConfig.jvmVer)

// dependencies and their resolvers

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

// for simulacrum
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

lazy val flinkVer = "0.9.0"

lazy val sparkVer = "1.4.0"

libraryDependencies ++= Seq(
  // flink
  "org.apache.flink" % "flink-scala"   % flinkVer,
  "org.apache.flink" % "flink-clients" % flinkVer,
  // spark
  "org.apache.spark" %% "spark-core" % sparkVer,
  // algebra, typeclasses, functional 
  "com.github.mpilquist" %% "simulacrum" % "0.4.0",
  "org.spire-math" %% "algebra" % "0.3.1",
  // Testing
  "org.scalatest" %% "scalatest" % "2.2.4" % Test
)

// publishing settings

Publish.settings(
  repo = Repository.github("malcolmgreaves", name.toString),
  developers =
    Seq(
      Dev("mgreaves", "Malcolm Greaves")
    ),
  art = ArtifactInfo.sonatype,
  lic = License.apache20
)

// unit test configuration

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

testOptions in Test += Tests.Argument("-oF")

fork in Test := true

parallelExecution in Test := false

