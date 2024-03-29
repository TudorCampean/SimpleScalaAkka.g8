import sbt._
import sbt.Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import com.typesafe.sbt.SbtScalariform._
import sbt.Project.Initialize

//
// Build setup
//
object $name$Build extends Build {

	//
	// Settings
	//
	lazy val defaultSettings = Defaults.defaultSettings ++ 
		scalariformSettings ++ 
		Seq(
		
		// Info
		organization := "$organization$",
		version := "$version$",
		scalaVersion := "$scala_version$",

		// Repositories
		resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
		resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
		resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
		resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    
		// Compile options
		scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize", "-feature"),
		javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    
		// sbtEclipse - see examples https://github.com/typesafehub/sbteclipse/blob/master/sbteclipse-plugin/src/sbt-test/sbteclipse/02-contents/project/Build.scala
		EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.Unmanaged, EclipseCreateSrc.Source, EclipseCreateSrc.Resource),
		EclipseKeys.withSource := true,
		
		// Run options with GC
		fork in run := true,
		javaOptions in run ++= Seq("-server", "-Xms1G", "-Xmx2G","-Xss1m","-XX:+CMSClassUnloadingEnabled", "-XX:MaxPermSize=64m", "-XX:+UseParallelGC", "-XX:+AggressiveOpts", "-XX:+UseFastAccessorMethods")	
	)
  
	//
	// Projects
	//
	lazy val $name$ = Project(id = "$name$",
							base=file(""),
                           settings = defaultSettings ++ Seq(
                           libraryDependencies ++= Dependencies.$name$
						))	
}

//
// Dependencies
//
object Dependencies {
	object Dependency {
		val akkaActor     = "com.typesafe.akka"      %% "akka-actor"         % "2.1.0" 
	}
	
	import Dependency._
	
	val $name$ = Seq(akkaActor)	
}

	

