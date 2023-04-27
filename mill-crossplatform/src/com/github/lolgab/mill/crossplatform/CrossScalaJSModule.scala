package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalajslib._

trait CrossScalaJSModule extends ScalaJSModule with VersionSpecific.CrossScalaJSModule {
  override def scalaJSVersion = crossScalaJSVersion
}
