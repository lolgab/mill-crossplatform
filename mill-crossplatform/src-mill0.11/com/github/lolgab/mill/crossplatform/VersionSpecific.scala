package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalalib._

private[crossplatform] object VersionSpecific {
  def getModules[M <: CrossPlatformAndScala](cross: Cross[M]) =
    cross.crossModules
  object IsCross {
    def unapply[M <: CrossPlatformAndScala](
        cross: Cross[M]
    ): Option[Cross[M]] = Some(
      cross
    )
  }
  trait CrossPlatformScalaModule extends ScalaModule {}
}
