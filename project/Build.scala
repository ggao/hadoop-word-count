import sbt._
import sbt.Keys._
import scala._
import com.github.retronym.SbtOneJar


object BuildSettings {

  import Dependencies._

  val crossHadoopVersions = SettingKey[Seq[String]](
    "cross-hadoop-versions",
    "The versions of Hadoop used for cross building."
  )

  val hadoopVersion = SettingKey[String](
    "hadoop-version",
    "The version of Hadoop used for building."
  )

  lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
    name := "HadoopWordCount",
    version := "0.1",
    organization := "com.dbtsai",
    scalaVersion := "2.9.2",
    exportJars := true,
    crossPaths := false,
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
    testListeners <<= (target, streams).map((t, s) => Seq(new eu.henkelmann.sbt.JUnitXmlTestsListener(t.getAbsolutePath))),
    hadoopVersion := "2.0.0-mr1-cdh4.2.0",
    crossHadoopVersions := Seq("2.0.0-mr1-cdh4.2.0"),
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
      name := "mapreduce",
      libraryDependencies ++= hadoopDependencies
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
  val HADOOP_VERSION = "2.0.0-mr1-cdh4.2.0"

  lazy val sharedLibraryDependencies = Seq(
    "com.novocode" % "junit-interface" % "0.10-M4" % "test",
    "eu.henkelmann" % "junit_xml_listener" % "0.2" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "log4j" % "log4j" % "1.2.17"
  )

  def matchHadoopDependencies(x: String): Seq[ModuleID] = x match {
    case "2.0.0-mr1-cdh4.2.0" =>
      Seq(
        "org.apache.hadoop" % "hadoop-core" % "2.0.0-mr1-cdh4.2.0",
        "org.apache.hadoop" % "hadoop-client" % "2.0.0-mr1-cdh4.2.0",
        // "org.apache.hadoop" % "hadoop-hdfs" % "2.0.0-mr1-cdh4.2.0",
        // "org.apache.hadoop" % "hadoop-minicluster" % "2.0.0-mr1-cdh4.2.0" % "test",
        "org.apache.mrunit" % "mrunit" % "0.9.0-incubating" % "test" classifier "hadoop2"
      )
    case x => sys.error("Unsupported Hadoop version " + x)
  }

  lazy val hadoopDependencies = matchHadoopDependencies(HADOOP_VERSION)
}
