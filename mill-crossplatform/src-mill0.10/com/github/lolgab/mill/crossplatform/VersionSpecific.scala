package com.github.lolgab.mill.crossplatform

import mill._
import mill.define.Cross.Resolver
import mill.scalajslib._
import mill.scalalib._
import mill.scalanativelib._

import scala.language.reflectiveCalls

private[crossplatform] object VersionSpecific {
  def getModules[T](cross: Cross[T]) = cross.items.map(_._2)
  object IsCross {
    def unapply(cross: Cross[_]): Option[Cross[_]] = Some(cross)
  }
  trait CrossPlatformScalaModule extends ScalaModule {
    override def millSourcePath = super.millSourcePath / os.up
  }
}
