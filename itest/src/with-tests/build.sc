import $file.plugins

import mill._
import mill.api.Result
import mill.eval._
import mill.main.MainModule
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule {
  def scalaVersion = "3.3.1"
}
trait CommonJVM extends Common
trait CommonJS extends Common with ScalaJSModule {
  def scalaJSVersion = "1.14.0"
}
trait CommonNative extends Common with ScalaNativeModule {
  def scalaNativeVersion = "0.4.16"
}
trait CommonTests extends TestModule.Munit {
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.scalameta::munit::1.0.0-M10"
  )
}
object core extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule {
    trait SharedTests extends CommonTests
  }
  object jvm extends Shared with CommonJVM {
    object test extends ScalaTests with SharedTests
  }
  object js extends Shared with CommonJS {
    object test extends ScalaJSTests with SharedTests
  }
  object native extends Shared with CommonNative {
    object test extends ScalaNativeTests with SharedTests
  }
}

def verify(ev: Evaluator) = T.command {
  ev.evalOrThrow()(core.jvm.test.test())
  ev.evalOrThrow()(core.js.test.test())
  ev.evalOrThrow()(core.native.test.test())

  ()
}
