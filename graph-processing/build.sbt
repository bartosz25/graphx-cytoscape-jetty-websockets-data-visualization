name := "graphx-realtime-visualization"

version := "1.0"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

libraryDependencies += "org.apache.spark" %% "spark-core" % s"${sparkVersion}"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % s"${sparkVersion}"
libraryDependencies += "javax.websocket" % "javax.websocket-api" % "1.0"
libraryDependencies += "org.eclipse.jetty.websocket" % "javax-websocket-client-impl" % s"9.2.7.v20150116"
