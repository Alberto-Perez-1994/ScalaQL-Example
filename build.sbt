

lazy val settings = Seq(

  scalaVersion := "2.13.4",

  libraryDependencies ++= Seq("org.xerial" % "sqlite-jdbc" % "3.14.2",
                              "io.github.alberto-perez-1994" %% "scalaql" % "1.0.0"),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
)


lazy val app = project
  .in(file("."))
  .settings(
    name := "app",
    settings
  )
