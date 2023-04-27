# CrossPlatform Mill Plugin

Mill Plugin to simplify Cross platform Mill projects

## Getting Started

Here you can see a basic example using mill-crossplatform

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.1.2`
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule {
  def scalaVersion = "2.13.10"
}

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.12"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.13.1"
}

object core extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule with Common {
    // common `core` settings here
  }
  object jvm extends Shared {
    // jvm specific settings here
  }
  object js extends Shared with CommonJS {
    // js specific settings here
  }
  object native extends Shared with CommonNative {
    // native specific settings here
  }
}

object other extends CrossPlatform {
  // root moduleDeps are correctly applied
  // to all platform submodules
  def moduleDeps = Seq(core)
  trait Shared extends CrossPlatformScalaModule with Common {
    // common `other` settings here
  }
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}
```

## Advanced use cases

## Platform specific code

You can place platform specific code in:

```scala
// jvm specific code
millSourcePath / "jvm" / "src"

// directory used in js and jvm modules but not in native
// the directory names are sorted alphabetically.
// it's `js-jvm`, not `jvm-js`.
millSourcePath / "js-jvm" / "src"

// code shared between js and native
// used when the scalaVersion is a Scala 3
// version
millSourcePath / "js-native" / "src-3"
```


### Supporting multiple Scala versions

It is possible to use `CrossPlatform` together with `Cross`
to cross-compile for multiple Scala versions:

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.1.2`
import com.github.lolgab.mill.crossplatform._

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.12"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.13.1"
}

val scalaVersions = Seq("2.13.10", "3.2.1")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  // Note `CrossPlatformCrossScalaModule` instead of `CrossPlatformScalaModule`
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object app extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  def moduleDeps = Seq(core())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}
```

### Supporting multiple Scala.js / Native versions

It is possible to use `CrossPlatform` together with `Cross`
in the inner modules to cross-compile for multiple Scala.js / Scala Native versions.
Root `moduleDeps` and `compileModuleDeps` work as expected

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.1.2`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.0", "3.2.1")
val scalaJSVersions = Seq("1.13.1")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  // the cross-module should have only one parameter named `val crossScalaJSVersion: String`
  // for it to work correctly. Extend `CrossScalaJSModule` which requires it.
  class JSModule(val crossScalaJSVersion: String) extends Shared with CrossScalaJSModule
  object js extends Cross[JSModule](scalaJSVersions: _*)
}
```

### Disabling platforms dynamically

It is possible to disable a platform dynamically.
This is useful, for example, when a platform doesn't support a certain Scala version.

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.1.2`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.0", "3.2.1")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  
  // Enable Scala Native only for Scala 2
  def enableNative = crossScalaVersion.startsWith("2.")

  object jvm extends Shared
  object js extends Shared with ScalaJSModule {
    def scalaJSVersion = "1.13.1"
  }
  object native extends Shared with ScalaNativeModule {
    def scalaNativeVersion = "0.4.9"
  }
}
```
