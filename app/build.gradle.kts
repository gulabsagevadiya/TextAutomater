plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "custom.automater.textautomater"
  compileSdk = 36

  defaultConfig {
    applicationId = "custom.automater.textautomater"
    minSdk = 24
    targetSdk = 36
    versionCode = 8
    versionName = "8.0.0"

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
  buildFeatures {
    compose = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("com.google.code.gson:gson:2.13.2")
  implementation("com.google.android.material:material:1.13.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")

  // Compose BOM
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Compose UI
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // Material 3
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")

  // Activity Compose
  implementation("androidx.activity:activity-compose:1.12.2")

  // Lifecycle
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

  // Hilt
  implementation("com.google.dagger:hilt-android:2.56")
  ksp("com.google.dagger:hilt-android-compiler:2.56")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}

tasks.register("printVersionName") {
  println("v${android.defaultConfig.versionName}")
}