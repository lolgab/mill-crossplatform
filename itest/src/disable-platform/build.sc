// import $file.plugins

import mill._
// import mill.api.Result
// import mill.eval._
// import mill.main.MainModule
// import mill.scalalib._
// import mill.scalajslib._
// import mill.scalanativelib._
// import com.github.lolgab.mill.crossplatform._

// trait CommonJVM extends ScalaModule {
//   def scalaVersion = "2.13.10"
// }
// trait CommonJS extends CommonJVM with ScalaJSModule {
//   def scalaJSVersion = "1.12.0"
// }
// object jvm extends CrossPlatform {
//   def enableJVM = false
//   object jvm extends CrossPlatformScalaModule with CommonJVM
// }
// object js extends CrossPlatform {
//   def enableJS = false
//   object js extends CrossPlatformScalaModule with CommonJS
// }
// object native extends CrossPlatform {
//   def enableNative = false
//   object native extends Cross[Native]("0.4.9")
//   trait Native
//       extends CrossPlatformScalaModule
//       with CommonJVM
//       with CrossScalaNativeModule
// }

// def execute(ev: Evaluator, command: String) = {
//   MainModule.evaluateTasks(
//     ev,
//     Seq(command),
//     SelectMode.Single
//   )(identity)
// }

// def verify(ev: Evaluator) = T.command {
//   locally {
//     val Result.Failure(message, _) = execute(ev, "native.__.scalaNativeVersion")
//     assert(
//       message.startsWith("Cannot resolve native.scalaNativeVersion"),
//       s"Wrong message: $message"
//     )
//   }

//   locally {
//     val Result.Failure(message, _) = execute(ev, "js._.scalaJSVersion")
//     assert(
//       message.startsWith("Cannot resolve js._.scalaJSVersion"),
//       s"Wrong message: $message"
//     )
//   }

//   locally {
//     val Result.Failure(message, _) = execute(ev, "jvm._.scalaJSVersion")
//     assert(
//       message.startsWith("Cannot resolve jvm._.scalaJSVersion"),
//       s"Wrong message: $message"
//     )
//   }
// }

def verify() = T.command {}
