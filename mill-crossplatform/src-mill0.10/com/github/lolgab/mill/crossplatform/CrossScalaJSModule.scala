package com.github.lolgab.mill.crossplatform

import mill._
import mill.define.Cross.Resolver
import mill.scalajslib._
import mill.scalalib._

import scala.language.reflectiveCalls

trait CrossScalaJSModule extends ScalaJSModule {
  def crossScalaJSVersion: String
  override def scalaJSVersion = crossScalaJSVersion
  override def millSourcePath = super.millSourcePath / os.up
  override def artifactName: T[String] = T {
    super.artifactName().split('-').init.mkString("-")
  }
}
