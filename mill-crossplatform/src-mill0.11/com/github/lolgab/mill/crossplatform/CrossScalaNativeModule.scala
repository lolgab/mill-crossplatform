package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalanativelib._

trait CrossScalaNativeModule
    extends ScalaNativeModule
    with Cross.Module[String] {
  def crossScalaNativeVersion: String = crossValue
  override def scalaNativeVersion = crossScalaNativeVersion
  override def artifactName: T[String] = T {
    super.artifactName().split('-').init.mkString("-")
  }
}
