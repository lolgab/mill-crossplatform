import $exec.plugins

import mill._
import mill.define.SelectMode
import mill.main.MainModule
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import mill.eval._
import com.github.lolgab.mill.crossplatform._

trait CommonJVM extends ScalaModule {
  def scalaVersion = "2.13.10"
}
trait CommonJS extends CommonJVM with ScalaJSModule {
  def scalaJSVersion = "1.12.0"
}
trait CommonNative extends CommonJVM with ScalaNativeModule {
  def scalaNativeVersion = "0.4.9"
}
object jvm extends CrossPlatform {
  def enableJVM = false
  object jvm extends CrossPlatformScalaModule with CommonJVM
}
object js extends CrossPlatform {
  def enableJS = false
  object js extends CrossPlatformScalaModule with CommonJS
}
object native extends CrossPlatform {
  def enableNative = false
  object native extends CrossPlatformScalaModule with CommonNative
}

def execute(ev: Evaluator, command: String) = {
  MainModule.evaluateTasks(
    ev,
    Seq(command),
    SelectMode.Single
  )(identity)
}

def verifyNative(ev: Evaluator) = T.command {
  execute(ev, "native._.scalaNativeVersion")
}
def verifyJS(ev: Evaluator) = T.command {
  execute(ev, "js._.scalaJSVersion")
}
def verifyJVM(ev: Evaluator) = T.command {
  execute(ev, "jvm._.scalaVersion")
}
