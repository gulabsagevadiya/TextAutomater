plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "custom.automater.textautomater"
  compileSdk = 36

  defaultConfig {
    applicationId = "custom.automater.textautomater"
    minSdk = 24
    targetSdk = 36
    versionCode = 7
    versionName = "2.0.7"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.16.0")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("com.google.code.gson:gson:2.13.1")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}

tasks.register("printVersionName") {
  println("v${android.defaultConfig.versionName}")
}