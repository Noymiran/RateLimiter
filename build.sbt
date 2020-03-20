name := "RateLimiter"

version := "0.1"

scalaVersion := "2.13.1"

val akkaVersion = "2.6.4"
val akkaFullVersion = "10.1.11"
val akkaMajorVersion = "2.6"
val slf4jVersion = "1.7.30"
val scalaTest="3.1.1"

lazy val akka = Seq(

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaFullVersion
)

lazy val logging = Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion
)

lazy val guava = Seq("com.revinate" % "guava-rate-limiter" % "19.0")

lazy val tests = Seq(
  "org.mockito" %% "mockito-scala-scalatest" % "1.13.0" % Test,
  "org.scalatest" %% "scalatest" % scalaTest % Test,
  "org.scalactic" %% "scalactic" % scalaTest % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.3" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaFullVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test)

libraryDependencies ++= akka ++ logging ++ guava ++ tests