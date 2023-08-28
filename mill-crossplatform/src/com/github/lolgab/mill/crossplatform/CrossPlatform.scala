package com.github.lolgab.mill.crossplatform

import mill._
import mill.define.DynamicModule
import mill.main.BuildInfo
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

import scala.language.reflectiveCalls

trait CrossPlatform extends Cross.Module[String] with DynamicModule {
  container =>
  override def crossValue: String = throw new Exception(
    """CrossPlatform defines `def crossValue: String` only because it's required by Cross.Module[String].
      |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
  )

  private[crossplatform] protected def myArtifactNameParts: Seq[String] =
    millModuleSegments.parts

  CrossPlatform.checkMillVersion()
  def moduleDeps: Seq[CrossPlatform] = Seq.empty
  def compileModuleDeps: Seq[CrossPlatform] = Seq.empty

  def enableJVM: Boolean = true
  def enableJS: Boolean = true
  def enableNative: Boolean = true

  private def platforms: Seq[String] = {
    def loop(module: Module): Seq[String] = module match {
      case _: ScalaNativeModule => Seq("native")
      case _: ScalaJSModule     => Seq("js")
      case _: ScalaModule       => Seq("jvm")
      case c: Cross[_] =>
        loop(
          c.crossModules.head.asInstanceOf[Module]
        )
      case _ => Seq.empty[String]
    }
    millInternal
      .reflectNestedObjects[Module]()
      .flatMap(m => loop(m))
      .toSeq
  }

  private def enableModuleCondition(module: Module): Boolean = module match {
    case _: ScalaNativeModule => enableNative
    case _: ScalaJSModule     => enableJS
    case _: ScalaModule       => enableJVM
    case c: Cross[_] =>
      enableModuleCondition(
        c.crossModules.head.asInstanceOf[Module]
      )
    case _ => true
  }
  override def millModuleDirectChildren: Seq[Module] =
    super.millModuleDirectChildren
      .filter(enableModuleCondition)

  trait CrossPlatformScalaModule extends ScalaModule {
    override def artifactNameParts = container.myArtifactNameParts
    override def millSourcePath = super.millSourcePath / os.up
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

    trait CrossPlatformSources extends ScalaTests {
      override def sources = T.sources {
        super.sources() ++ platformSources(millSourcePath)
      }
    }

    private type WithDirNames = {
      def scalaVersionDirectoryNames: Seq[String]
    }

    private def platformCombinations = {
      val platforms = container.platforms
      if (platforms.length <= 2) Seq.empty[Seq[String]]
      else platforms.combinations(2).toSeq
    }
    private def platformSources(baseDir: os.Path) = {
      Agg(
        PathRef(baseDir / platform / "src")
      ) ++ platformCombinations
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
              ) ++ platformCombinations
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

  // copy pasted from https://github.com/com-lihaoyi/mill/blob/6247fafc43c1d7dd58e36e920c010b0997832c02/scalalib/src/CrossModuleBase.scala#L17
  implicit def crossPlatformResolver: Cross.Resolver[CrossPlatform] =
    new Cross.Resolver[CrossPlatform] {
      def resolve[V <: CrossPlatform](c: Cross[V]): V = {
        val scalaV = crossValue
        scalaV
          .split('.')
          .inits
          .takeWhile(_.length > 1)
          .flatMap(prefix =>
            c.crossModules
              .find(_.crossValue.split('.').startsWith(prefix))
          )
          .collectFirst { case x => x }
          .getOrElse(
            throw new Exception(
              s"Unable to find compatible cross version between $scalaV and " +
                c.crossModules
                  .map(_.crossValue)
                  .mkString(",")
            )
          )
      }
    }

  trait CrossPlatformCrossScalaModule
      extends CrossScalaModule
      with CrossPlatformScalaModule {
    override def artifactNameParts = container.myArtifactNameParts.init
    override def crossValue: String = throw new Exception(
      """CrossPlatformCrossScalaModule defines `def crossValue: String` only because it's required by CrossScalaModule (defined in Cross.Module[String]).
      |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
    )
    override def crossScalaVersion: String = myCrossValue
    private[crossplatform] protected def myCrossValue: String =
      container.crossValue
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

  private def checkMillVersion() = {
    val isMillVersionOk = BuildInfo.millVersion match {
      case _ => true
    }
    require(
      isMillVersionOk,
      s"Minimum supported version of Mill is `0.11.0`. You are using `${BuildInfo.millVersion}`."
    )
  }
}
