import mill._

import mill.scalalib._
import mill.scalalib.api.Util.scalaNativeBinaryVersion
import mill.scalalib.publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.6.1`
import de.tobiasroeser.mill.integrationtest._
import $ivy.`com.goyeau::mill-scalafix::0.2.11`
import com.goyeau.mill.scalafix.ScalafixModule
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.3.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import os.Path

val millVersions = Seq("0.11.0")
val millBinaryVersions = millVersions.map(scalaNativeBinaryVersion)

def millBinaryVersion(millVersion: String) = scalaNativeBinaryVersion(
  millVersion
)
def millVersion(binaryVersion: String) =
  millVersions.find(v => millBinaryVersion(v) == binaryVersion).get

trait CommonPublish extends PublishModule {
  def pomSettings = PomSettings(
    description = "Mill Plugin to ease Cross Platform projects",
    organization = "com.github.lolgab",
    url = "https://github.com/lolgab/mill-crossplatform",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("lolgab", "mill-crossplatform"),
    developers = Seq(
      Developer("lolgab", "Lorenzo Gabriele", "https://github.com/lolgab")
    )
  )
  def publishVersion = VcsVersion.vcsState().format()
}

object `mill-crossplatform`
    extends Cross[MillcrossplatformCross](millBinaryVersions: _*)
class MillcrossplatformCross(millBinaryVersion: String)
    extends ScalaModule
    with CommonPublish
    with ScalafixModule {
  override def millSourcePath = super.millSourcePath / os.up
  override def artifactName = s"mill-crossplatform_mill$millBinaryVersion"

  override def sources = T.sources {
    super.sources() ++ Seq(
      millSourcePath / s"src-mill${millVersion(millBinaryVersion).split('.').take(2).mkString(".")}"
    ).map(PathRef(_))
  }
  def scalaVersion = BuildInfo.scalaVersion
  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalalib:${millVersion(millBinaryVersion)}",
    ivy"com.lihaoyi::mill-scalanativelib:${millVersion(millBinaryVersion)}",
    ivy"com.lihaoyi::mill-scalajslib:${millVersion(millBinaryVersion)}"
  )

  def scalacOptions =
    super.scalacOptions() ++ Seq("-Ywarn-unused", "-deprecation", "-feature")

  def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:0.6.0")
}

object itest extends Cross[itestCross]("0.11.0")
class itestCross(millVersion: String) extends MillIntegrationTestModule {
  override def millSourcePath: Path = super.millSourcePath / os.up
  def millTestVersion = millVersion
  def pluginsUnderTest = Seq(
    `mill-crossplatform`(millBinaryVersion(millVersion))
  )
  def testBase = millSourcePath / "src"
}
