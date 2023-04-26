import $file.plugins

import mill._
import mill.api.Result
import mill.define.SelectMode
import mill.eval._
import mill.main.MainModule
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.10", "3.2.2")

trait CommonJVM extends ScalaModule
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.12.0"
}
trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.7"
}

object other extends Cross[OtherCross](scalaVersions)
trait OtherCross extends CrossPlatformAndScala {
  def moduleDeps = Seq(core(crossScalaVersion))
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object `no-native` extends Cross[NoNativeCross](scalaVersions)
trait NoNativeCross extends CrossPlatformAndScala {
  def moduleDeps = Seq(other())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
}

object core extends Cross[CoreModule](scalaVersions)
trait CoreModule extends CrossPlatformAndScala {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object crossScalaJSNative extends Cross[CrossScalaJSNative](scalaVersions)
trait CrossScalaJSNative extends CrossPlatformAndScala {
  def moduleDeps = Seq(core(crossScalaVersion))
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared with CommonJVM
  object js extends Cross[JS]("1.13.1")
  trait JS extends Shared with CrossScalaJSModule
  object native extends Cross[Native]("0.4.12")
  trait Native extends Shared with CrossScalaNativeModule
}

def execute(ev: Evaluator, command: String)(f: ujson.Value => Unit) = {
  MainModule.evaluateTasks(
    ev,
    Seq(command),
    SelectMode.Single
  )(v => f(v.head._2.arr.head))
}

def verify(ev: Evaluator) = T.command {
  `no-native`("3.2.2").jvm.compile()

  locally {
    val Result.Success(_) = execute(ev, "core[3.2.2].jvm.artifactName") {
      case ujson.Str(result) =>
        assert(
          result == "core",
          s"Wrong artifactName: $result"
        )
    }
  }

  locally {
    val Result.Success(_) = execute(ev, "crossScalaJSNative[3.2.2].native[0.4.12].artifactName") {
      case ujson.Str(result) =>
        assert(
          result == "crossScalaJSNative",
          s"Wrong artifactName: $result"
        )
    }
  }

  locally {
    val Result.Success(_) = execute(ev, "crossScalaJSNative[3.2.2].js[1.13.1].artifactName") {
      case ujson.Str(result) =>
        assert(
          result == "crossScalaJSNative",
          s"Wrong artifactName: $result"
        )
    }
  }

  ()
}
