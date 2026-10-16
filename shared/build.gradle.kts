import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  // The Android-KMP plugin adds no lint task for the main sources; this plugin restores `lint` (and its `check` hook).
  alias(libs.plugins.androidLint)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

kotlin {
  android {
    namespace = "com.chukchukhaksa.mobile.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    // Widget XML and Compose resources are packaged only when Android resources are enabled.
    androidResources {
      enable = true
    }
    // Without a host test compilation, commonTest does not run on Android.
    withHostTest {
      // Presenter tests run Compose (Molecule) on the JVM, and the runtime calls android.os/android.util stubs
      // (Trace, Looper, Log) that throw "not mocked" unless they return defaults.
      isReturnDefaultValues = true
    }

    lint {
      // A library has no targetSdk of its own; lint against the app's target, as before the split.
      targetSdk = libs.versions.android.targetSdk.get().toInt()
      disable += "NullSafeMutableLiveData"
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.preview)
      implementation(libs.androidx.activity.compose)
      implementation(libs.koin.android)
      implementation(libs.firebase.common)
      implementation(libs.firebase.database.ktx)
      implementation(libs.firebase.config.ktx)
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.firebase.crashlytics.ktx)
      implementation(libs.firebase.analytics.ktx)
      implementation(libs.glance.appwidget)
      implementation(libs.glance.material3)
      implementation(libs.compose.runtime)
      implementation(libs.kakao.sdk.v2.user)
      implementation(libs.ktor.client.okhttp)
      implementation(libs.amplitude.analytics.android)
      implementation(libs.play.services.ads)
      implementation(libs.androidx.graphics.path)
    }
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.sqlite.bundled)
    }
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodel)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.kotlinx.immutable)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
      implementation(libs.compose.runtime)

      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.koin.compose.viewmodel.navigation)

      implementation(libs.circuit.foundation)

      implementation(libs.androidx.room.runtime)

      implementation(libs.androidx.datastore.preferences)

      implementation(libs.kmp.firebase.database)
      implementation(libs.kmp.firebase.config)
      implementation(libs.kmp.firebase.crashlytics)
      implementation(libs.kmp.firebase.analytics)
      implementation(libs.napier)
      implementation(libs.kinappbrowser)

      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.client.auth)

      implementation(libs.ksafe)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
      implementation(libs.circuit.test)
    }
  }
}

room {
  schemaDirectory("$projectDir/schemas")
}

compose.resources {
  // Keep the Res package independent of the module name.
  packageOfResClass = "chukchukhaksa.composeapp.generated.resources"
}

dependencies {
  "androidRuntimeClasspath"(compose.uiTooling)
  kspKmp(libs.androidx.room.compiler)
}

fun DependencyHandlerScope.kspKmp(
  artifact: Provider<MinimalExternalModuleDependency>,
) {
  add("kspAndroid", artifact)
  add("kspIosArm64", artifact)
  add("kspIosSimulatorArm64", artifact)
}
