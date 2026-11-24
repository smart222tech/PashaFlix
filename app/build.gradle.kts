plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
}
android {
 namespace = "com.smart222tech.lampa"
 compileSdk = 35
 defaultConfig {
  applicationId = "com.smart222tech.lampa"
  minSdk = 28
  targetSdk = 35
  versionCode = 2
  versionName = "1.1"
  ndk { abiFilters += "armeabi-v7a" }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
}
