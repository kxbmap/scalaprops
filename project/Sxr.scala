import sbt._, Keys._
import sbtunidoc.Plugin._

object Sxr {

  val enableSxr = SettingKey[Boolean]("enableSxr")
  val sxr = TaskKey[File]("packageSxr")

  private[this] def ifSxrAvailable[A](key: SettingKey[A], value: Def.Initialize[A]): Setting[A] =
    key <<= (key, enableSxr, value){ (k, enable, vv) =>
      if (enable) {
        vv
      } else {
        k
      }
    }

  private[this] def ifSxrAvailable[A](key: TaskKey[A], value: Def.Initialize[Task[A]]): Setting[Task[A]] =
    key := {
      if (enableSxr.value) {
        value.value
      } else {
        key.value
      }
    }

  val settings1 = Seq[Setting[_]](
    enableSxr := { scalaVersion.value.startsWith("2.12") == false },
    ifSxrAvailable(
      scalacOptions in UnidocKeys.unidoc,
      Def.task {
        (scalacOptions in UnidocKeys.unidoc).value ++ Seq(
          "-P:sxr:base-directory:" + (sources in UnidocKeys.unidoc in ScalaUnidoc).value.mkString(":")
        )
      }
    )
  )

  val settings2: Seq[Setting[_]] = Defaults.packageTaskSettings(
    sxr in Compile, (crossTarget in Compile, UnidocKeys.unidoc in Compile).map{ (dir, _) =>
      Path.allSubpaths(dir / "unidoc.sxr").toSeq
    }
  ) ++ Seq[Setting[_]](
    ifSxrAvailable(
      resolvers,
      Def.setting(resolvers.value :+ ("bintray/paulp" at "https://dl.bintray.com/paulp/maven"))
    ),
    ifSxrAvailable(
      libraryDependencies,
      Def.setting(libraryDependencies.value :+ compilerPlugin("org.improving" %% "sxr" % "1.0.1"))
    ),
    ifSxrAvailable(
      packagedArtifacts,
      Def.task(packagedArtifacts.value ++ Classpaths.packaged(Seq(sxr in Compile)).value)
    ),
    ifSxrAvailable(
      artifacts,
      Def.setting(artifacts.value ++ Classpaths.artifactDefs(Seq(sxr in Compile)).value)
    ),
    ifSxrAvailable(
      artifactClassifier in sxr,
      Def.setting(Option("sxr"))
    )
  )

}
