package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._

trait CrossPlatform extends Module { container =>
  def crossScalaVersion: String
  def moduleDeps: Seq[CrossPlatform] = Seq.empty
  def compileModuleDeps: Seq[CrossPlatform] = Seq.empty

  trait CrossPlatformBase extends CrossScalaModule {
    override def millSourcePath = super.millSourcePath / os.up
    override def crossScalaVersion: String = container.crossScalaVersion
    override def moduleDeps = super.moduleDeps ++
      container.moduleDeps.map(innerModule)
    override def compileModuleDeps = super.compileModuleDeps ++
      container.compileModuleDeps.map(innerModule)

    private def innerModule(mod: CrossPlatform) = this match {
      case _: ScalaNativeModule => CrossPlatform.toNative(mod)
      case _: ScalaJSModule     => CrossPlatform.toJS(mod)
      case _: ScalaModule       => CrossPlatform.toJVM(mod)
    }
  }
}
object CrossPlatform {
  private type tpe = {
    def native: ScalaNativeModule
    def js: ScalaJSModule
    def jvm: ScalaModule
  }
  private def toNative(mod: CrossPlatform): ScalaNativeModule = {
    try {
      mod.asInstanceOf[tpe].native
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(s"module $mod doesn't contain a native module")
    }
  }
  private def toJS(mod: CrossPlatform): ScalaJSModule = {
    try {
      mod.asInstanceOf[tpe].js
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(s"module $mod doesn't contain a js module")
    }
  }
  private def toJVM(mod: CrossPlatform): ScalaModule = {
    try {
      mod.asInstanceOf[tpe].jvm
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(s"module $mod doesn't contain a jvm module")
    }
  }
}
