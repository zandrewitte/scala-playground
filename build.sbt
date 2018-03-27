import sbt.Keys._

// -----------------------------------------------------------------------------
// Common Settings
// -----------------------------------------------------------------------------
name := "ScalaPlayground"
organization := "com.zantech"
scalaVersion := library.Version.scala
crossScalaVersions := Seq(scalaVersion.value, library.Version.scala)
organization := "com.zantech"
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding",
  "UTF-8",
  "-feature",
  "-Xfatal-warnings"
)
unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value)
unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value)
parallelExecution in Test := false

// -----------------------------------------------------------------------------
// Library dependencies
// -----------------------------------------------------------------------------

libraryDependencies ++= Seq(
  library.akkaActor,
  library.akkaHttp,
  library.akkaStream,
  library.akkaStreamCirce,
  library.cassandra,
  library.circe,
  library.mockito,
  library.scalaTest,
  library.scalaz,
  library.shapeless
)

lazy val library = new {

  object Version {
    val akka            = "2.5.9"
    val akkaHttp        = "10.1.0"
    val akkaStream      = "2.5.11"
    val akkaStreamCirce = "3.5.0"
    val cassandraDriver = "3.2.0"
    val circe           = "0.9.0"
    val mockito   = "2.7.19"
    val scala     = "2.12.4"
    val scalaz    = "7.2.18"
    val scalaTest = "3.0.1"
    val shapeless = "2.3.2"
  }

  val akkaActor  = "com.typesafe.akka"      %% "akka-actor"           % Version.akka
  val akkaHttp   = "com.typesafe.akka"      %% "akka-http"            % Version.akkaHttp
  val akkaStream = "com.typesafe.akka"      %% "akka-stream"          % Version.akkaStream
  val akkaStreamCirce = "de.knutwalker" %% "akka-stream-circe" % akkaStreamCirce
  val cassandra  = "com.datastax.cassandra" % "cassandra-driver-core" % Version.cassandraDriver
  val mockito    = "org.mockito"            % "mockito-core"          % Version.mockito % Test
  val circe      = "io.circe"               % "circe-generic"         % Version.circe
  val scalaTest = "org.scalatest" %% "scalatest"   % Version.scalaTest % Test
  val scalaz    = "org.scalaz"    %% "scalaz-core" % Version.scalaz
  val shapeless = "com.chuusai"   %% "shapeless"   % Version.shapeless

}

// -----------------------------------------------------------------------------
// Wartremover settings
// -----------------------------------------------------------------------------

wartremoverErrors in (Compile, compile) ++= Warts.allBut(
  Wart.Any,
  Wart.ImplicitParameter,
  Wart.NonUnitStatements
)
wartremoverExcluded ++= sourceManaged.value.**("*.scala").get

// -----------------------------------------------------------------------------
// Scala Format settings
// -----------------------------------------------------------------------------

scalafmtOnCompile := true
scalafmtOnCompile.in(Sbt) := true
scalafmtVersion := "1.4.0"
