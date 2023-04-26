package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._
import mill.scalanativelib._

trait CrossScalaNativeModule extends ScalaNativeModule {
  def crossScalaNativeVersion: String
  override def scalaNativeVersion = crossScalaNativeVersion
  override def millSourcePath = super.millSourcePath / os.up
  override def artifactName: T[String] = T {
    super.artifactName().split('-').init.mkString("-")
  }
}
