package com.github.lolgab.mill.crossplatform

import mill._
import mill.define.Cross
import mill.scalalib._
import mill.scalajslib._

trait CrossPlatformAndScala extends CrossPlatform { container =>

  def crossScalaVersion: String

  // copy pasted from https://github.com/com-lihaoyi/mill/blob/6247fafc43c1d7dd58e36e920c010b0997832c02/scalalib/src/CrossModuleBase.scala#L17
  implicit def crossPlatformResolver: Cross.Resolver[CrossPlatformAndScala] =
    new Cross.Resolver[CrossPlatformAndScala] {
      def resolve[V <: CrossPlatformAndScala](c: Cross[V]): V = {
        val scalaV = getCrossScalaVersion
        scalaV
          .split('.')
          .inits
          .takeWhile(_.length > 1)
          .flatMap(prefix =>
            VersionSpecific
              .getModules(c)
              .find(_.getCrossScalaVersion.split('.').startsWith(prefix))
          )
          .collectFirst { case x => x }
          .getOrElse(
            throw new Exception(
              s"Unable to find compatible cross version between $scalaV and " +
                VersionSpecific
                  .getModules(c)
                  .map(_.getCrossScalaVersion)
                  .mkString(",")
            )
          )
      }
    }

  trait CrossPlatformCrossScalaModule
      extends CrossPlatformScalaModule
      with CrossScalaModule {
    override def artifactName: T[String] =
      millModuleSegments.parts.dropRight(2).mkString("-")

    override def crossScalaVersion: String = container.crossScalaVersion
  }

  private type WithCrossScalaVersion = {
    def crossScalaVersion: String
  }
  private def getCrossScalaVersion: String = {
    try {
      this.asInstanceOf[WithCrossScalaVersion].crossScalaVersion
    } catch {
      case _: NoSuchMethodException =>
        throw new Exception(
          s"$this doesn't define `val crossScalaVersion: String`."
        )
    }
  }
}
