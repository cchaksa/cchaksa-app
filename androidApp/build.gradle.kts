import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { load(it) }
  }
}

fun localProperty(key: String): String = localProperties.getProperty(key, "")

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.googleService)
  alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

android {
  namespace = "com.chukchukhaksa.mobile"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.kunize.uswtimetable"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 58
    versionName = "3.2.2"

    buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${localProperty("KAKAO_NATIVE_APP_KEY")}\"")
    manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperty("KAKAO_NATIVE_APP_KEY")

    buildConfigField("String", "AMPLITUDE_API_KEY_DEV", "\"${localProperty("AMPLITUDE_API_KEY_DEV")}\"")
    buildConfigField("String", "AMPLITUDE_API_KEY_PROD", "\"${localProperty("AMPLITUDE_API_KEY_PROD")}\"")
  }
  packaging {
    jniLibs {
      useLegacyPackaging = false
      // Bundled SQLite is iOS-only; keep the prebuilt .so out of the Android APK.
      excludes += "**/libsqliteJni.so"
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("debug") {
      manifestPlaceholders["ADMOB_APP_ID"] = localProperty("ADMOB_APP_ID_TEST")
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperty("ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST")}\"")
    }
    getByName("release") {
      isMinifyEnabled = false
      manifestPlaceholders["ADMOB_APP_ID"] = localProperty("ADMOB_APP_ID_PROD")
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperty("ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD")}\"")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }
  buildFeatures {
    buildConfig = true
  }
  lint {
    disable += "NullSafeMutableLiveData"
  }
}

dependencies {
  implementation(projects.composeApp)

  implementation(compose.runtime)
  implementation(compose.ui)
  implementation(compose.preview)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.kakao.sdk.v2.user)
  implementation(libs.play.services.ads)
  implementation(project.dependencies.platform(libs.firebase.bom))
  implementation(libs.firebase.common)
  implementation(libs.napier)

  coreLibraryDesugaring(libs.desugar.jdk.libs)
  debugImplementation(compose.uiTooling)
}
