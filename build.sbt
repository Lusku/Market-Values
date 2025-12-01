ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "MarketValues"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.2.1",
  "org.apache.spark" %% "spark-sql" % "3.2.1",
  "org.json4s" %% "json4s-jackson" % "3.6.11",
  "org.json4s" %% "json4s-core" % "3.6.11",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.knowm.xchart" % "xchart" % "3.8.0",
  "org.apache.poi" % "poi" % "5.2.3",
  "org.apache.poi" % "poi-ooxml" % "5.2.3"
)

