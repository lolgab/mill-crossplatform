package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._
import mill.define.Cross

trait CrossPlatformCrossScala extends CrossPlatform with Cross.Module[String] {
  container =>

  def crossScalaVersion: String = crossValue

  // copy pasted from https://github.com/com-lihaoyi/mill/blob/6247fafc43c1d7dd58e36e920c010b0997832c02/scalalib/src/CrossModuleBase.scala#L17
  implicit def crossPlatformResolver: Cross.Resolver[CrossPlatformCrossScala] =
    new Cross.Resolver[CrossPlatformCrossScala] {
      def resolve[V <: CrossPlatformCrossScala](c: Cross[V]): V = {
        val scalaV = crossScalaVersion
        scalaV
          .split('.')
          .inits
          .takeWhile(_.length > 1)
          .flatMap(prefix =>
            VersionSpecific
              .getModules(c)
              .find(_.crossScalaVersion.split('.').startsWith(prefix))
          )
          .collectFirst { case x => x }
          .getOrElse(
            throw new Exception(
              s"Unable to find compatible cross version between $scalaV and " +
                VersionSpecific
                  .getModules(c)
                  .map(_.crossScalaVersion)
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

    override def crossValue: String = container.crossScalaVersion
  }
}
