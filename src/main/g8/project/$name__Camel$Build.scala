import sbt._
import sbt.Keys._

object $name;format="Camel"$Build extends Build {

  lazy val $name;format="camel"$ = Project(
    id = "$name;format="norm"$",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "$name$",
      organization := "$organization$",
      version := "$version$",
      scalaVersion := "$scala_version$",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
	  resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
	  resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
	  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
	  libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3",
	  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.6",
	  libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.7",
	  libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7",
	  libraryDependencies += "org.mongodb" % "casbah_2.9.2" % "2.4.1" ,
	  javaOptions in run += "-server -Xms256M -Xmx256M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=64M -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
    )
  )
}
