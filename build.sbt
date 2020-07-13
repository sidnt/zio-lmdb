import BuildHelper._
import explicitdeps.ExplicitDepsPlugin.autoImport.moduleFilterRemoveValue
import sbtcrossproject.CrossPlugin.autoImport.crossProject

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-lmdb/")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "sidnt",
        "sidnt",
        "11618157+sidnt@users.noreply.github.com",
        url("http://sidnt.github.io")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/zio/zio-lmdb/"),
        "scm:git:git@github.com:zio/zio-lmdb.git"
      )
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "check",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

val zioVersion = "1.0.0-RC21-2"

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    unusedCompileDependenciesFilter -= moduleFilter(
      "org.scala-js",
      "scalajs-library"
    )
  )
  .aggregate(
    zioLmdbJVM,
    zioLmdbJS
  )

lazy val zioLmdb = crossProject(
  JSPlatform,
  JVMPlatform
)
  .in(file("zio-lmdb"))
  .settings(stdSettings("zioLmdb"))
  .settings(crossProjectSettings)
  .settings(
    buildInfoSettings("zio.lmdb")
  )
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    )
  )
  .settings(
    testFrameworks += new TestFramework(
      "zio.test.sbt.ZTestFramework"
    )
  )

lazy val zioLmdbJS = zioLmdb.js
  .settings(
    scalaJSUseMainModuleInitializer := true
  )

lazy val zioLmdbJVM = zioLmdb.jvm
  .settings(dottySettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.lmdbjava" % "lmdbjava" % "0.8.1"
    )
  )

lazy val docs = project
  .in(file("zio-lmdb-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-lmdb-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(
      root
    ),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite
      .dependsOn(unidoc in Compile)
      .value,
    docusaurusPublishGhpages := docusaurusPublishGhpages
      .dependsOn(unidoc in Compile)
      .value
  )
  .dependsOn(root)
  .enablePlugins(
    MdocPlugin,
    DocusaurusPlugin,
    ScalaUnidocPlugin
  )
