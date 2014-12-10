lazy val root = (project in file(".")).
  settings(
    name := "WikiCycles",
    version := "1.0",
    scalaVersion := "2.11.4",
    fork in run := true,
    javaOptions in run += "-Xmx8G",
    libraryDependencies ++=  Seq("org.scalatest" %% "scalatest" % "2.2.2" % "test",
                "info.bliki.wiki" % "bliki-core" % "3.0.19",
                "commons-io" % "commons-io" % "2.4")
  )