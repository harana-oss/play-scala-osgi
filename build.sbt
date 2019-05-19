
scalaVersion := "2.12.8"

ThisBuild / organization := "com.harana"

ThisBuild / version := "1.0"

ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

lazy val PlayScalaOSGI = (project in file(".")).enablePlugins(PlayScala).settings(
  name := "play-scala-osgi",

  bintrayOrganization := Some("harana"),

  libraryDependencies ++= Seq(
    "org.apache.felix" % "org.apache.felix.framework" % "5.6.12"
  )
)