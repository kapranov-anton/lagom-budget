organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val budget = (project in file("."))
  .aggregate(`budget-api`, `budget-impl`)

lazy val `budget-api` = (project in file("budget-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `budget-impl` = (project in file("budget-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
//      lagomScaladslPersistenceJdbc,
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`budget-api`)
