import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    load(localPropertiesFile.inputStream())
  }
}

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
  alias(libs.plugins.googleService)
  alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  listOf(
    iosX64(),
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
      implementation(libs.androidx.core.splashscreen)
      implementation(libs.amplitude.analytics.android)
      implementation(libs.play.services.ads)
    }
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
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

      implementation(libs.androidx.room.runtime)
      implementation(libs.sqlite.bundled)

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
    }
  }
}

room {
  schemaDirectory("$projectDir/schemas")
}

android {
  namespace = "com.chukchukhaksa.mobile"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.kunize.uswtimetable"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 56
    versionName = "3.2.0"

    buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${localProperties["KAKAO_NATIVE_APP_KEY"] ?: ""}\"")
    manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperties["KAKAO_NATIVE_APP_KEY"] ?: ""

    buildConfigField("String", "AMPLITUDE_API_KEY_DEV", "\"${localProperties["AMPLITUDE_API_KEY_DEV"] ?: ""}\"")
    buildConfigField("String", "AMPLITUDE_API_KEY_PROD", "\"${localProperties["AMPLITUDE_API_KEY_PROD"] ?: ""}\"")
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("debug") {
      manifestPlaceholders["ADMOB_APP_ID"] = localProperties["ADMOB_APP_ID_TEST"] ?: ""
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperties["ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST"] ?: ""}\"")
    }
    getByName("release") {
      isMinifyEnabled = false
      manifestPlaceholders["ADMOB_APP_ID"] = localProperties["ADMOB_APP_ID_PROD"] ?: ""
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperties["ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD"] ?: ""}\"")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

  dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
  }

  buildFeatures {
    buildConfig = true
  }

  lint {
    disable += "NullSafeMutableLiveData"
  }
}

dependencies {
  debugImplementation(compose.uiTooling)
  kspKmp(libs.androidx.room.compiler)
}

fun DependencyHandlerScope.kspKmp(
  artifact: Provider<MinimalExternalModuleDependency>,
) {
  add("kspAndroid", artifact)
  add("kspIosX64", artifact)
  add("kspIosArm64", artifact)
  add("kspIosSimulatorArm64", artifact)
}
