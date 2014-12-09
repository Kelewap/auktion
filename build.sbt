name := "auktion"

version := "1.0"

scalaVersion := "2.10.4"

val akkaV = "2.3.6"
val sprayV = "1.3.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.2-SNAP2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.6",
  "io.spray"            %%  "spray-can"     % sprayV,
  "io.spray"            %%  "spray-routing" % sprayV,
  "io.spray"            %%  "spray-client"  % sprayV,
  "io.spray"            %%  "spray-testkit" % sprayV  % "test",
  "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
  "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
  "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
)

    