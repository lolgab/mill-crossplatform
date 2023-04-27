package com.github.lolgab.mill.crossplatform

import mill.define.Cross
import mill.scalalib._

private[crossplatform] object VersionSpecific {
  def getModules[M <: CrossPlatform](cross: Cross[M]) = cross.crossModules
  object IsCross {
    def unapply[M <: CrossPlatform](
        cross: Cross[M]
    ): Option[Cross[M]] = Some(
      cross
    )
  }
  trait CrossPlatformCrossScalaModule extends CrossScalaModule {
    override def crossValue: String = myCrossValue
    private[crossplatform] protected def myCrossValue: String
    override def artifactNameParts =
      super.artifactNameParts().patch(crossWrapperSegments.size - 1, Nil, 1)
  }
  trait CrossPlatform extends Cross.Module[String] {
    override def crossValue: String = throw new Exception(
      """CrossPlatform defines `def crossValue: String` only because it's required by Cross.Module[String].
      |You shouldn't need to use it directly. If you see this message please report a bug at https://github.com/lolgab/mill-crossplatform/issues/new""".stripMargin
    )

    private[crossplatform] protected def myCrossValue: String = crossValue
  }
}
