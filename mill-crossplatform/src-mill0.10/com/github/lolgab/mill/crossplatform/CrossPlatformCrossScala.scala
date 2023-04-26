package com.github.lolgab.mill.crossplatform

import mill._
import mill.define.Cross
import mill.scalalib._
import mill.scalajslib._

trait CrossPlatformCrossScala extends CrossPlatform { container =>

  // copy pasted from https://github.com/com-lihaoyi/mill/blob/6247fafc43c1d7dd58e36e920c010b0997832c02/scalalib/src/CrossModuleBase.scala#L17
  implicit def crossPlatformResolver: Cross.Resolver[CrossPlatformCrossScala] =
    new Cross.Resolver[CrossPlatformCrossScala] {
      def resolve[V <: CrossPlatformCrossScala](c: Cross[V]): V = {
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
