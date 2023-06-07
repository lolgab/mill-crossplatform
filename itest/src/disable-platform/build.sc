import $file.plugins

import mill._
import mill.api.Result
import mill.eval._
import mill.main.MainModule
import mill.resolve.SelectMode
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

trait CommonJVM extends ScalaModule {
  def scalaVersion = "2.13.10"
}
trait CommonJS extends CommonJVM with ScalaJSModule {
  def scalaJSVersion = "1.12.0"
}
object jvm extends CrossPlatform {
  def enableJVM = false
  object jvm extends CrossPlatformScalaModule with CommonJVM
}
object js extends CrossPlatform {
  object jvm extends CrossPlatformScalaModule with CommonJVM
  def enableJS = false
  object js extends CrossPlatformScalaModule with CommonJS
}
object native extends CrossPlatform {
  override def enableNative = false
  object native extends Cross[Native]("0.4.9")
  trait Native
      extends CrossPlatformScalaModule
      with CommonJVM
      with CrossScalaNativeModule
}

def execute(ev: Evaluator, task: String) = {
  mill.main.RunScript.evaluateTasksNamed(
    ev,
    Seq(task),
    selectMode = SelectMode.Separated
  )
}

def verify(ev: Evaluator) = T.command {
  locally {
    val message =
      execute(ev, "native.__.scalaNativeVersion").swap.getOrElse(???)
    assert(
      message.startsWith("Cannot resolve native.__.scalaNativeVersion"),
      s"Wrong message: $message"
    )
  }

  locally {
    val message = execute(ev, "js._.scalaJSVersion").swap.getOrElse(???)
    assert(
      message.startsWith("Cannot resolve js._.scalaJSVersion"),
      s"Wrong message: $message"
    )
  }

  locally {
    val result = execute(ev, "js.jvm.scalaVersion")
      .getOrElse(???)
      ._2
      .getOrElse(???)
      .head
      ._2
      .get
      ._2
      .str
    assert(result == "2.13.10", s"Wrong result: $result")
  }

  locally {
    val message = execute(ev, "jvm._.scalaVersion").swap.getOrElse(???)
    assert(
      message.startsWith("Cannot resolve jvm._.scalaVersion"),
      s"Wrong message: $message"
    )
  }
}
