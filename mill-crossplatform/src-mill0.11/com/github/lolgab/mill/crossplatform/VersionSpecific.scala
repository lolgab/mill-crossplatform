package com.github.lolgab.mill.crossplatform

import mill.define.Cross
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

private[crossplatform] object VersionSpecific {
  def getModules[M <: CrossPlatform](cross: Cross[M]) = cross.crossModules
  object IsCross {
    def unapply[M <: CrossPlatform](
        cross: Cross[M]
    ): Option[Cross[M]] = Some(
      cross
    )
  }
  trait CrossPlatformScalaModule extends ScalaModule {
    private[crossplatform] protected def myArtifactNameParts: Seq[String]
    override def artifactNameParts = myArtifactNameParts
  }
  trait CrossPlatformCrossScalaModule extends CrossScalaModule {
    override def crossValue: String = throw new Exception(
      """CrossPlatformCrossScalaModule defines `def crossValue: String` only because it's required by CrossScalaModule (defined in Cross.Module[String]).
      |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
    )
    override def crossScalaVersion: String = myCrossValue
    private[crossplatform] protected def myCrossValue: String
  }
  trait CrossPlatform extends Cross.Module[String] {
    override def crossValue: String = throw new Exception(
      """CrossPlatform defines `def crossValue: String` only because it's required by Cross.Module[String].
      |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
    )

    private[crossplatform] protected def myCrossValue: String = crossValue
    private[crossplatform] protected def myArtifactNameParts: Seq[String] =
      millModuleSegments.parts.init
  }

  trait CrossScalaJSModule extends ScalaJSModule with Cross.Module[String] {
    def crossScalaJSVersion: String = crossValue
  }

  trait CrossScalaNativeModule
      extends ScalaNativeModule
      with Cross.Module[String] {
    def crossScalaNativeVersion: String = crossValue
    override def scalaNativeVersion = crossScalaNativeVersion
  }
}
