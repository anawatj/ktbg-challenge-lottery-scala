

name := "ktbg-challenge-lottery-scala"

version := "0.1"
enablePlugins(FlywayPlugin)
val AkkaVersion = "2.8.6"
val AkkaHttpVersion = "10.5.3"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.6" % Runtime,
  "com.typesafe.slick" %% "slick" % "3.5.1",
  "org.postgresql" % "postgresql" % "42.7.3",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.pauldijou" %% "jwt-core" % "5.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
  "org.slf4j" % "slf4j-nop" % "2.0.13",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
)

flywayDriver := "org.postgresql.Driver"
flywayUrl := "jdbc:postgresql://localhost:5432/lottery?user=postgres&password=1234"
flywayUser := "postgres"
flywayPassword := "1234"
flywayLocations += "db/migration"
