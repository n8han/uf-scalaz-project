import sbt._
import Keys._

object Stream extends Build {
  lazy val project =
    Project("Project", file("."), settings = Defaults.defaultSettings ++ Seq(
      scalaVersion := "2.8.1",
      libraryDependencies += ("org.clapper" %% "avsl" % "0.3.1")
    )
  ) dependsOn (
    ProjectRef(file("../unfiltered"), "unfiltered-scalaz"),
    ProjectRef(file("../unfiltered"), "unfiltered-jetty"),
    ProjectRef(file("../unfiltered"), "unfiltered-filter")
  )
}
