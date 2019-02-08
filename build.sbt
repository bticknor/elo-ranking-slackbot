name := "scala-slack-bot-example"

scalaVersion := "2.11.6"

version := "0.1"

resolvers += "scalac repo" at "https://raw.githubusercontent.com/ScalaConsultants/mvn-repo/master/"

libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"

libraryDependencies ++= Seq(
    "net.debasishg" %% "redisclient" % "3.9"
)

