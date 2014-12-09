lazy val root = (project in file(".")).
  settings(
    name := "WikiCycles",
    version := "1.0",
    scalaVersion := "2.11.4",
    //fork in run := true,
    libraryDependencies ++=  Seq("org.scalatest" %% "scalatest" % "2.2.2" % "test",
                "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
                "info.bliki.wiki" % "bliki-core" % "3.0.19",
                "commons-io" % "commons-io" % "2.4")
  )