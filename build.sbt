enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "ServerTeleSupp"

version := "0.3"

scalaVersion := "2.12.10"

dockerBaseImage := "openjdk:8-jre-alpine"
packageName in Docker := "servertelesupp"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,

  "com.typesafe.slick" %% "slick" % "3.3.1",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
  "org.postgresql" % "postgresql" % "42.2.5" //org.postgresql.ds.PGSimpleDataSource dependency
)

//mainClass in(Compile, run) := Some("com.mardaunt.DBobject")