# CrossPlatform Mill Plugin

Mill Plugin to simplify Cross platform Mill projects

## Getting Started

Here you can see a basic example using mill-crossplatform

```scala
//| mill-version: 1.0.0
//| mvnDeps:
//| - com.github.lolgab::mill-crossplatform::0.3.0

import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule {
  def scalaVersion = "3.7.0"
}

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.5.7"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.19.0"
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
to cross-compile for multiple Scala versions.

#### With Mill 0.11

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`
import com.github.lolgab.mill.crossplatform._

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.15"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.14.0"
}

val scalaVersions = Seq("2.13.12", "3.3.1")

object core extends Cross[CoreModule](scalaVersions)
trait CoreModule extends CrossPlatform {
  // Note `CrossPlatformCrossScalaModule` instead of `CrossPlatformScalaModule`
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object app extends Cross[AppModule](scalaVersions)
trait AppModule extends CrossPlatform {
  def moduleDeps = Seq(core())
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}
```

#### With Mill 0.10

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.2`
import com.github.lolgab.mill.crossplatform._

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.15"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.14.0"
}

val scalaVersions = Seq("2.13.12", "3.3.1")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  // Note `CrossPlatformCrossScalaModule` instead of `CrossPlatformScalaModule`
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}

object app extends Cross[AppModule](scalaVersions: _*)
class AppModule(val crossScalaVersion: String) extends CrossPlatform {
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

#### With Mill 0.11

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.12", "3.3.1")
val scalaJSVersions = Seq("1.14.0")

object core extends Cross[CoreModule](scalaVersions)
trait CoreModule extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Cross[JSModule](scalaJSVersions)
  trait JSModule extends Shared with CrossScalaJSModule
}
```

#### With Mill 0.10

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.2`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.12", "3.3.1")
val scalaJSVersions = Seq("1.14.0")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  // the cross-module should have only one parameter named `val crossScalaJSVersion: String`
  // for it to work correctly. Extend `CrossScalaJSModule` which requires it.
  object js extends Cross[JSModule](scalaJSVersions: _*)
  class JSModule(val crossScalaJSVersion: String) extends Shared with CrossScalaJSModule
}
```

### Disabling platforms dynamically

It is possible to disable a platform dynamically.
This is useful, for example, when a platform doesn't support a certain Scala version.

#### With Mill 0.11

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.12", "3.3.1")

object core extends Cross[CoreModule](scalaVersions)
trait CoreModule extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  
  // Enable Scala Native only for Scala 2
  def enableNative = crossValue.startsWith("2.")

  object jvm extends Shared
  object js extends Shared with ScalaJSModule {
    def scalaJSVersion = "1.14.0"
  }
  object native extends Shared with ScalaNativeModule {
    def scalaNativeVersion = "0.4.12"
  }
}
```

#### With Mill 0.10

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.2`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.12", "3.3.1")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  
  // Enable Scala Native only for Scala 2
  def enableNative = crossScalaVersion.startsWith("2.")

  object jvm extends Shared
  object js extends Shared with ScalaJSModule {
    def scalaJSVersion = "1.14.0"
  }
  object native extends Shared with ScalaNativeModule {
    def scalaNativeVersion = "0.4.12"
  }
}
```

### Test modules

For tests, you need to have platform specific test modules

#### With Mill 0.11

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule {
  def scalaVersion = "3.3.1"
}
trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.15"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.14.0"
}
trait CommonTests extends TestModule.Munit {
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.scalameta::munit::1.0.0-M10"
  )
}

object core extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule with Common {
    // common `core` settings here
    trait SharedTests extends CommonTests {
      // common `core` test settings here
    }
  }
  object jvm extends Shared {
    // jvm specific settings here
    object test extends ScalaTests with SharedTests
  }
  object js extends Shared with CommonJS {
    // js specific settings here
    object test extends ScalaJSTests with SharedTests
  }
  object native extends Shared with CommonNative {
    // native specific settings here
    object test extends ScalaNativeTests with SharedTests
  }
}
```
