import sbt._
import sbt.Keys._
import scala._
import com.github.retronym.SbtOneJar

object BuildSettings {

  import Dependencies._

  lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
    name := "HadoopWordCount",
    version := "0.1",
    organization := "com.dbtsai",
    scalaVersion := "2.9.2",
    exportJars := true,
    crossPaths := false,
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    resolvers ++= Seq(
      "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
    ),
    libraryDependencies ++= sharedLibraryDependencies
  )
}

object HadoopWordCountBuild extends Build {

  import BuildSettings._
  import Dependencies._

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = sharedSettings ++ Seq(
      name := "root"
    ) ++ SbtOneJar.oneJarSettings
  ) aggregate(mapreduce, utility)

  lazy val mapreduce = Project(
    id = "mapreduce",
    base = file("mapreduce"),
    settings = sharedSettings ++ Seq(
      name := "mapreduce"
    )
  ) dependsOn (utility)

  lazy val utility = Project(
    id = "utility",
    base = file("utility"),
    settings = sharedSettings ++ Seq(
      name := "utility",
      libraryDependencies ++= hadoopDependencies
    )
  )
}

object Dependencies {
  val HADOOP_VERSION = "2.0.0-mr1-cdh4.1.1"

  //  val excludeJackson = ExclusionRule(organization = "org.codehaus.jackson")
  //  val excludeNetty = ExclusionRule(organization = "org.jboss.netty")

  lazy val sharedLibraryDependencies = Seq(
    "log4j" % "log4j" % "1.2.17",
    "org.mockito" % "mockito-all" % "1.9.5" % "test"
  )

  lazy val hadoopDependencies = Seq(
    "org.apache.hadoop" % "hadoop-core" % HADOOP_VERSION,
    "org.apache.hadoop" % "hadoop-client" % HADOOP_VERSION
    //"org.apache.mrunit" % "mrunit" % "0.9.0-incubating" % "test"
  )
}