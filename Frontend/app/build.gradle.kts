plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cloudmonitor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cloudmonitor.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0"

        // ── API base URL ──────────────────────────────────────────────────────
        // 10.0.2.2 = localhost alias inside the Android Emulator
        // For a physical device, replace with your machine's LAN IP e.g. "http://192.168.1.100:3000/"
        buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:3000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production URL example:
            // buildConfigField("String", "BASE_URL", "\"https://api.yourapp.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ADD THESE TWO (fix tabIndicatorOffset error)
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")

    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Async
    implementation(libs.kotlinx.coroutines.android)

    // Storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
}