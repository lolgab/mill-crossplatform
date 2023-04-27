package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._

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
  trait CrossPlatformCrossScalaModule extends CrossScalaModule {
    override def millSourcePath = super.millSourcePath / os.up

    override def crossScalaVersion: String = myCrossValue
    private[crossplatform] protected def myCrossValue: String

    override def artifactName: T[String] =
      millModuleSegments.parts.dropRight(2).mkString("-")

  }

  trait CrossPlatform {
    private[crossplatform] def myCrossValue: String = getCrossScalaVersion(this)
  }
}
