import $file.plugins

import mill._
import mill.api.Result
import mill.eval._
import mill.main.MainModule
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.12", "3.3.1")

trait CommonJVM extends ScalaModule
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.12.0"
}
trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.7"
}

object normal extends CrossPlatform {
  object jvm extends CrossPlatformScalaModule {
    def scalaVersion = "3.3.1"
  }
}

object other extends Cross[OtherCross](scalaVersions)
trait OtherCross extends CrossPlatform {
  def moduleDeps = Seq(core())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object `no-native` extends Cross[NoNativeCross](scalaVersions)
trait NoNativeCross extends CrossPlatform {
  def moduleDeps = Seq(other())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
}

object core extends Cross[CoreModule](scalaVersions)
trait CoreModule extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object crossScalaJSNative extends Cross[CrossScalaJSNative](scalaVersions)
trait CrossScalaJSNative extends CrossPlatform {
  def moduleDeps = Seq(core())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Cross[JS]("1.14.0")
  trait JS extends Shared with CrossScalaJSModule
  object native extends Cross[Native]("0.4.15")
  trait Native extends Shared with CrossScalaNativeModule
}

def verify(ev: Evaluator) = T.command {
  `no-native`("3.3.1").jvm.compile()

  locally {
    val result = ev.evalOrThrow()(normal.jvm.artifactName)
    assert(
      result == "normal",
      s"Wrong artifactName: $result"
    )
  }

  locally {
    val result = ev.evalOrThrow()(core("3.3.1").jvm.artifactName)
    assert(
      result == "core",
      s"Wrong artifactName: $result"
    )
  }

  locally {
    val result = ev.evalOrThrow()(
      crossScalaJSNative("3.3.1").native("0.4.15").artifactName
    )
    assert(
      result == "crossScalaJSNative",
      s"Wrong artifactName: $result"
    )
  }

  locally {
    val result =
      ev.evalOrThrow()(crossScalaJSNative("3.3.1").js("1.14.0").artifactName)
    assert(
      result == "crossScalaJSNative",
      s"Wrong artifactName: $result"
    )
  }

  locally {
    val result =
      ev.evalOrThrow()(crossScalaJSNative("3.3.1").js("1.14.0").scalaVersion)
    assert(
      result == "3.3.1",
      s"Wrong artifactName: $result"
    )
  }

  locally {
    val path = ev.evalOrThrow()(normal.jvm.sources).head.path / os.up
    assert(
      path.last == "normal",
      s"Wrong millSourcePath: $path"
    )
  }

  locally {
    val path = ev.evalOrThrow()(core("3.3.1").jvm.sources).head.path / os.up
    assert(
      path.last == "core",
      s"Wrong millSourcePath: $path"
    )
  }

  ()
}
