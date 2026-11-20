plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smart222tech.atodo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smart222tech.atodo"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    // Fire TV Stick 4K Max (1st gen) exposes a 32-bit ARM ABI.
    // Keep a separate ARM64 build for modern Android phones.
    flavorDimensions += "abi"
    productFlavors {
        create("firetv") {
            dimension = "abi"
            ndk { abiFilters += "armeabi-v7a" }
        }
        create("phone") {
            dimension = "abi"
            ndk { abiFilters += "arm64-v8a" }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    add("firetvImplementation", "org.mozilla.geckoview:geckoview-armeabi-v7a:140.0.20250707120347")
    add("phoneImplementation", "org.mozilla.geckoview:geckoview-arm64-v8a:140.0.20250707120347")
}
