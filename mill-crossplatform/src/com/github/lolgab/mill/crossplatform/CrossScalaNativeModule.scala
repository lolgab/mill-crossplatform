package com.github.lolgab.mill.crossplatform

import mill._
import mill.scalanativelib._

trait CrossScalaNativeModule
    extends ScalaNativeModule
    with VersionSpecific.CrossScalaNativeModule {
  override def scalaNativeVersion = crossScalaNativeVersion
}
