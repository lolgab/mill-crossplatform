import $exec.plugins

import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.8", "3.2.0")

trait CommonJVM extends ScalaModule
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.12.0"
}
trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.7"
}

object other extends Cross[OtherCross](scalaVersions: _*)
class OtherCross(val crossScalaVersion: String) extends CrossPlatform {
  def moduleDeps = Seq(core(crossScalaVersion))
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object `no-native` extends Cross[NoNativeCross](scalaVersions: _*)
class NoNativeCross(val crossScalaVersion: String) extends CrossPlatform {
  def moduleDeps = Seq(other(crossScalaVersion))
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
}

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

def verify() = T.command {
  `no-native`("3.2.0").jvm.compile()
  ()
}
