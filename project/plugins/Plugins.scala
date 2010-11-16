import sbt._


class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  // Repository
  val twitterRepo = "T repo" at "http://maven.twttr.com/"

  val twitterNest = "com.twitter" at "http://www.lag.net/nest"
  val defaultProject = "com.twitter" % "standard-project" % "0.7.12"

}
