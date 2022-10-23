package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

trait CrossScalaJSModule extends ScalaJSModule {
  def crossScalaJSVersion: String
  def scalaJSVersion = crossScalaJSVersion
}
trait CrossScalaNativeModule extends ScalaJSModule {
  def crossScalaNativeVersion: String
  def scalaNativeVersion = crossScalaNativeVersion
}

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
      case thisModule: ScalaNativeModule =>
        CrossPlatform
          .toNative(thisModule, mod)
          .asInstanceOf[this.type]
      case thisModule: ScalaJSModule =>
        CrossPlatform
          .toJS(thisModule, mod)
          .asInstanceOf[this.type]
      case _: ScalaModule => CrossPlatform.toJVM(mod).asInstanceOf[this.type]
    }
    def platform: String = this match {
      case _: ScalaNativeModule => "native"
      case _: ScalaJSModule     => "js"
      case _: ScalaModule       => "jvm"
    }
    override def sources = T.sources {
      super.sources() ++ platformSources(millSourcePath)
    }

    trait CrossPlatformSources extends Tests {
      override def sources = T.sources {
        super.sources() ++ platformSources(millSourcePath)
      }
    }

    private def platformSources(baseDir: os.Path) = {
      Agg(
        PathRef(baseDir / platform / "src")
      ) ++ CrossPlatform.platformCombinations
        .filter(_.contains(platform))
        .map(combination =>
          PathRef(
            baseDir / combination.mkString("-") / "src"
          )
        ) ++ scalaVersionDirectoryNames.flatMap(name =>
        Agg(
          PathRef(baseDir / platform / s"src-$name")
        ) ++ CrossPlatform.platformCombinations
          .filter(_.contains(platform))
          .map(combination =>
            PathRef(
              baseDir / combination.mkString("-") / s"src-$name"
            )
          )
      )
    }
  }
}
object CrossPlatform {
  private type tpe = {
    def native: AnyRef
    def js: AnyRef
    def jvm: ScalaModule
  }
  private def toNative(
      thisModule: ScalaNativeModule,
      mod: CrossPlatform
  ): ScalaNativeModule = {
    type CrossNative = { def crossScalaNativeVersion: String }
    try {
      mod.asInstanceOf[tpe].native match {
        case it: ScalaNativeModule => it
        case it: Cross[_] =>
          val scalaNativeV =
            try { thisModule.asInstanceOf[CrossNative].crossScalaNativeVersion }
            catch {
              case _: NoSuchMethodException =>
                throw new Exception(
                  s"Module $thisModule should define `val crossScalaNativeVersion: String`"
                )
            }
          it(scalaNativeV).asInstanceOf[ScalaNativeModule]
      }
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(s"module $mod doesn't contain a native module")
    }
  }
  private def toJS(
      thisModule: ScalaJSModule,
      mod: CrossPlatform
  ): ScalaJSModule = {
    type CrossJS = { def crossScalaJSVersion: String }

    try {
      mod.asInstanceOf[tpe].js match {
        case it: ScalaJSModule => it
        case it: Cross[_] =>
          val scalaJSV =
            try { thisModule.asInstanceOf[CrossJS].crossScalaJSVersion }
            catch {
              case _: NoSuchMethodException =>
                throw new Exception(
                  s"Module $thisModule should define `val crossScalaJSVersion: String`"
                )
            }
          it(scalaJSV).asInstanceOf[ScalaJSModule]
      }
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
  private val platformCombinations =
    Seq("js", "jvm", "native").combinations(2).toSeq
}
