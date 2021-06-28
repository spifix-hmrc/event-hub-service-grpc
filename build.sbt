import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator
import sbt.Keys.resolvers
import sbt.MavenRepository

lazy val `event-hub-service-grpc` = (project in file("."))
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(PlayScala)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(scalaVersion := "2.13.6")
  .settings(libraryDependencies ++= Dependencies.libraries)
  .settings(libraryDependencies += guice)
  .settings(akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator)
  .settings(akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client, AkkaGrpc.Server))
      