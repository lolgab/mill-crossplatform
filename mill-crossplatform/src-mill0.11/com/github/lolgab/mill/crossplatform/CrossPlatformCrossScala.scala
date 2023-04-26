package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._
import mill.define.Cross

trait CrossPlatformAndScala extends CrossPlatform with Cross.Module[String] {
  container =>

  def crossScalaVersion: String = crossValue

  // copy pasted from https://github.com/com-lihaoyi/mill/blob/6247fafc43c1d7dd58e36e920c010b0997832c02/scalalib/src/CrossModuleBase.scala#L17
  implicit def crossPlatformResolver: Cross.Resolver[CrossPlatformAndScala] =
    new Cross.Resolver[CrossPlatformAndScala] {
      def resolve[V <: CrossPlatformAndScala](c: Cross[V]): V = {
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
    override def artifactNameParts = super.artifactNameParts().dropRight(1)

    override def crossValue: String = throw new Exception(
      """CrossPlatformCrossScalaModule defines `def crossValue: String` only because it's required by CrossScalaModule (inherited from Cross.Module[String]).
        |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
    )
    override def crossScalaVersion: String = container.crossScalaVersion
  }
}
