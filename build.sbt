organization := "io.malcolmgreaves"

name := "demo_data"

version := "0.0.1"

// scala & java compilation & runtime settings


//                         :::   NOTE   :::
// Unfortunately...
// As of right now (2015/August/18 11:45PM PST), Flink doesn't cross compile to 2.11.
// There's support in master to do this, but they haven't pushed to maven central.
// Update to 2.11 ASAP! (JIRA ticket: https://issues.apache.org/jira/browse/FLINK-1760 ).

scalaVersion := "2.10.5"

crossScalaVersions := Seq(scalaVersion.value)

//                         :::   NOTE   :::
// we want to update to JVM 8 ASAP !
// since we know that we want to be able to use this stuff w/ Spark,
// we unfortunately have to limit ourselves to jvm 7.
// once this gets resolved, we'll update: 
// [JIRA Issue]     https://issues.apache.org/jira/browse/SPARK-6152

lazy val jvmVer = "1.7"

scalacOptions := Seq(
  "-optimize",
  "-Xfatal-warnings",
  "-feature",
  s"-target:jvm-$jvmVer",
  "-deprecation",
  "-encoding", "utf8",
  "-language:implicitConversions",
  "-language:existentials",
  "-language:higherKinds"
)

javacOptions := Seq(
 "-source", jvmVer,
 "-target", jvmVer
)

javaOptions := Seq(
 "-server",
 "-XX:+AggressiveOpts",
 "-XX:+UseG1GC",
 "-XX:+TieredCompilation"
)

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

// unit test configuration

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

testOptions in Test += Tests.Argument("-oF")

fork in Test := true

parallelExecution in Test := false

