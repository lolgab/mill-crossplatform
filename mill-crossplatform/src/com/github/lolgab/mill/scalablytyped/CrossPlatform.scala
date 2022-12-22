package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

import scala.language.reflectiveCalls

trait CrossScalaJSModule extends ScalaJSModule {
  def crossScalaJSVersion: String
  override def scalaJSVersion = crossScalaJSVersion
}
trait CrossScalaNativeModule extends ScalaNativeModule {
  def crossScalaNativeVersion: String
  override def scalaNativeVersion = crossScalaNativeVersion
}

trait CrossPlatform extends Module { container =>
  CrossPlatform.checkMillVersion()
  def moduleDeps: Seq[CrossPlatform] = Seq.empty
  def compileModuleDeps: Seq[CrossPlatform] = Seq.empty

  def enableJVM: Boolean = true
  def enableJS: Boolean = true
  def enableNative: Boolean = true

  private def enableModuleCondition(module: Module): Boolean = module match {
    case _: ScalaNativeModule => enableNative
    case _: ScalaJSModule     => enableJS
    case _: ScalaModule       => enableJVM
  }
  override lazy val millModuleDirectChildren: Seq[Module] =
    millInternal
      .reflectNestedObjects[Module]
      .filter(enableModuleCondition)
      .toSeq

  trait CrossPlatformCrossScalaModule
      extends CrossPlatformScalaModule
      with CrossScalaModule {
    private type WithCrossScalaVersion = {
      def crossScalaVersion: String
    }
    override def artifactName: T[String] =
      millModuleSegments.parts.dropRight(2).mkString("-")
    override def crossScalaVersion: String =
      try {
        container.asInstanceOf[WithCrossScalaVersion].crossScalaVersion
      } catch {
        case _: NoSuchMethodException =>
          throw new Exception(
            s"""$container should define `val crossScalaVersion: String`.
               |If you have a single Scala version use `extends CrossPlatformScalaModule`
               |instead of `extends CrossPlatformCrossScalaModule`""".stripMargin
          )
      }
  }
  trait CrossPlatformScalaModule extends ScalaModule {
    override def millSourcePath = super.millSourcePath / os.up
    override def artifactName: T[String] =
      millModuleSegments.parts.init.mkString("-")
    override def moduleDeps = super.moduleDeps ++
      container.moduleDeps.map(innerModule).asInstanceOf[Seq[this.type]]
    override def compileModuleDeps = super.compileModuleDeps ++
      container.compileModuleDeps.map(innerModule).asInstanceOf[Seq[this.type]]

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

    private type WithDirNames = {
      def scalaVersionDirectoryNames: Seq[String]
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
        ) ++ (this match {
        case _: CrossScalaModule =>
          this
            .asInstanceOf[WithDirNames]
            .scalaVersionDirectoryNames
            .flatMap(name =>
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
        case _ => Agg()
      })
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

  private def checkMillVersion() = {
    val isMillVersionOk = BuildInfo.millVersion match {
      case s"0.10.$n-$_" => n.toIntOption.exists(n => n >= 9)
      case s"0.10.$n"    => n.toIntOption.exists(n => n >= 9)
      case s"0.1$_"      => true
      case s"0.$_"       => false
    }
    require(
      isMillVersionOk,
      s"Minimum supported version of Mill is `0.10.9`. You are using `${BuildInfo.millVersion}`."
    )
  }
}
