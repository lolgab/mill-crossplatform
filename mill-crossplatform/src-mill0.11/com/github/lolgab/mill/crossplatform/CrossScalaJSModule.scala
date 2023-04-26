package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalajslib._

trait CrossScalaJSModule extends ScalaJSModule with Cross.Module[String] {
  def crossScalaJSVersion: String = crossValue
  override def scalaJSVersion = crossScalaJSVersion
  override def artifactName: T[String] = T {
    super.artifactName().split('-').init.mkString("-")
  }
}
