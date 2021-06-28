import sbt._

object Dependencies {

  object Version {
    val PlayGrpcRuntime = "0.9.1"
    val Cats = "2.6.0"
    val Akka = "2.6.14"
    val AkkaHttp = "10.2.3"
    val AlpakkaMongo = "3.0.1"
  }

  object Library {
    val PlayGrpcRuntime = "com.lightbend.play" %% "play-grpc-runtime"           % Version.PlayGrpcRuntime
    val Cats = "org.typelevel"                 %% "cats-core"                   % Version.Cats
    val AlpakkaMongo = "com.lightbend.akka"    %% "akka-stream-alpakka-mongodb" % Version.AlpakkaMongo
    val AkkaStream = "com.typesafe.akka"       %% "akka-stream"                 % Version.Akka

    val AkkaDiscovery = "com.typesafe.akka" %% "akka-discovery"             % Version.Akka
    val AkkaSlf4J = "com.typesafe.akka"     %% "akka-slf4j"                 % Version.Akka
    val AkkaTyped = "com.typesafe.akka"     %% "akka-actor-typed"           % Version.Akka
    val AkkaJackson = "com.typesafe.akka"   %% "akka-serialization-jackson" % Version.Akka

    val AkkaSpray = "com.typesafe.akka" %% "akka-http-spray-json" % Version.AkkaHttp
    val AkkaHttp2 = "com.typesafe.akka" %% "akka-http2-support"   % Version.AkkaHttp
  }

  import Library._

  val libraries: Seq[ModuleID] = Seq(
    PlayGrpcRuntime,
    Cats,
    AlpakkaMongo,
    AkkaStream,
    AkkaDiscovery,
    AkkaTyped,
    AkkaSlf4J,
    AkkaJackson,
    AkkaSpray,
    AkkaHttp2
  )
}
