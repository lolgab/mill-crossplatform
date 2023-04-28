package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

import scala.language.reflectiveCalls

private[crossplatform] object VersionSpecific {
  def getModules[T](cross: Cross[T]) = cross.items.map(_._2)
  object IsCross {
    def unapply(cross: Cross[_]): Option[Cross[_]] = Some(cross)
  }
  private type WithCrossScalaVersion = {
    def crossScalaVersion: String
  }
  private def getCrossScalaVersion(self: AnyRef): String = {
    try {
      self.asInstanceOf[WithCrossScalaVersion].crossScalaVersion
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(
          s"$self doesn't define `val crossScalaVersion: String`."
        )
    }
  }
  trait CrossPlatformScalaModule extends ScalaModule {
    private[crossplatform] protected def myArtifactNameParts: Seq[String]
    override def artifactName = myArtifactNameParts.mkString("-")
  }
  trait CrossPlatformCrossScalaModule extends CrossScalaModule {
    override def crossScalaVersion: String = myCrossValue
    private[crossplatform] protected def myCrossValue: String
  }

  trait CrossPlatform extends Module {
    private[crossplatform] protected def myCrossValue: String =
      getCrossScalaVersion(this)
    private[crossplatform] protected def myArtifactNameParts: Seq[String] =
      millModuleSegments.parts.init
  }

  trait CrossScalaJSModule extends ScalaJSModule {
    override def millSourcePath = super.millSourcePath / os.up
    def crossScalaJSVersion: String
  }

  trait CrossScalaNativeModule extends ScalaNativeModule {
    override def millSourcePath = super.millSourcePath / os.up
    def crossScalaNativeVersion: String
  }

}
