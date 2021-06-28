resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.bintrayRepo("playframework", "maven")

addSbtPlugin("com.typesafe.play"       % "sbt-plugin"         % "2.8.7")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc"      % "2.0.0")

libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % "0.9.1"
