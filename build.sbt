import ReleaseTransformations._
import UpdateReadme.updateReadme

val scala213Version = "2.13.3"
val defaultScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-Xlint",
  "-language:implicitConversions", "-language:higherKinds", "-language:existentials",
  "-unchecked"
)

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.y-yu",
    name := "atnos-eff-last-action",
    description := "Eff interpreter implementation for atnos Eff's `addLast`",
    homepage := Some(url("https://github.com/y-yu")),
    licenses := Seq("MIT" -> url(s"https://github.com/y-yu/atnos-eff-last-action/blob/master/LICENSE")),
    scalaVersion := scala213Version,
    scalacOptions ++= defaultScalacOptions,
    libraryDependencies ++= Seq(
      "org.atnos" %% "eff" % "5.12.0",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "org.atnos" %% "eff-monix" % "5.12.0" % "test"
    )
  )
  .settings(publishSettings)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  publishArtifact in Test := false,
  pomExtra :=
    <developers>
      <developer>
        <id>y-yu</id>
        <name>Yoshimura Hikaru</name>
        <url>https://github.com/y-yu</url>
      </developer>
    </developers>
      <scm>
        <url>git@github.com:y-yu/atnos-eff-last-action.git</url>
        <connection>scm:git:git@github.com:y-yu/atnos-eff-last-action.git</connection>
        <tag>{tagOrHash.value}</tag>
      </scm>,
  releaseTagName := tagName.value
)

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}