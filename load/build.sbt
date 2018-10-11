name := "load"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

enablePlugins(GatlingPlugin)

fork in run := true

javaOptions += "-Xmx1G"

libraryDependencies ++= {
  val gatlingV       = "2.2.2"
  Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingV % "test",
    "io.gatling"            % "gatling-test-framework"    % gatlingV % "test"
  )
}
