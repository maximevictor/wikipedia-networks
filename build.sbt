lazy val root = (project in file(".")).
  settings(
    name := "WikiCycles",
    libraryDependencies ++=  Seq("org.scalatest" %% "scalatest" % "2.2.2" % "test",
                "commons-io" % "commons-io" % "2.4")
  )