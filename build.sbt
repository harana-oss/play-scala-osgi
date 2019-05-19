organization := "com.harana"

name := "play-scala-osgi"

version := "1.0"

scalaVersion := "2.12.8"

publishMavenStyle := true

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

lazy val PlayScalaOSGI = (project in file(".")).enablePlugins(PlayScala).settings(
  libraryDependencies ++= Seq(
    "org.apache.felix" % "org.apache.felix.framework" % "5.6.12"
  )
)