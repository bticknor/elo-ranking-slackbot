name := "elo-bot"

scalaVersion := "2.11.6"

version := "1.2.1"

libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"

libraryDependencies ++= Seq(
    "net.debasishg" %% "redisclient" % "3.9"
)

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",   // akka actors
  "ch.qos.logback" % "logback-classic" % "1.1.3",  //logback, in order to log to file
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.1",   // needed for logback to work
  // and my other dependencies
)

lazy val root = (project in file(".")).enablePlugins(AssemblyPlugin)

