import sbt._
import com.twitter.sbt.StandardProject


//class QuerulousProject(info: ProjectInfo) extends StandardProject(info) {
class QuerulousProject(info: ProjectInfo) extends DefaultProject(info) {
  // Repository
  val twitterRepo = "T repo" at "http://maven.twttr.com/"

  val specs     = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5"
  val configgy  = "net.lag" % "configgy" % "2.0.2"
  val asm       = "asm" % "asm" %  "1.5.3"
  val cglib     = "cglib" % "cglib" % "2.1_3"
  val dbcp      = "commons-dbcp" % "commons-dbcp" % "1.2.2"
  val hamcrest  = "org.hamcrest" % "hamcrest-all" % "1.1"
  val jmock     = "org.jmock" % "jmock" % "2.4.0"
  val mysqljdbc = "mysql" % "mysql-connector-java" % "5.1.6"
  val objenesis = "org.objenesis" % "objenesis" % "1.1"
  val pool      = "commons-pool" % "commons-pool" % "1.3"
  val xrayspecs = "com.twitter" % "xrayspecs_2.8.0" % "2.1.1"
  val hsqldb    = "hsqldb"  % "hsqldb" % "1.8.0.7"
  val ScalaToolsSnap = ScalaToolsSnapshots
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"
  val mavenLocal = "Local Mavem" at "file://" + Path.userHome + "/.m2/repository"

  // ------------------------------------------------------------
  // publishing
  // ------------------------------------------------------------
  override def managedStyle = ManagedStyle.Maven
  val publishTo = Resolver.file("shared-repo", Path.userHome / "artifact-repository" / "m2" asFile)

  val sourceArtifact = Artifact(artifactID, "src", "jar", Some("sources"), Nil, None)

  override def packageDocsJar = defaultJarPath("-javadoc.jar")
  override def packageSrcJar= defaultJarPath("-sources.jar")
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)

  override def pomExtra =
    <inceptionYear>2010</inceptionYear>
    <url>http://www.traveas.com</url>
    <organization>
      <name>Traveas AB</name>
      <url>http://www.traveas.com</url>
    </organization>
    <licenses>
      <license>
        <name>Private</name>
        <distribution>repo</distribution>
      </license>
    </licenses>

}
