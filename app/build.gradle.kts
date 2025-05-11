plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Needed for Room Database compiler
}

android {
    namespace = "com.thuve.myrupees"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thuve.myrupees"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        viewBinding = true
        compose = true
    }
}

dependencies {
    // Core + Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)

    // RecyclerView, CardView, ConstraintLayout
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase (optional Crashlytics)
    implementation(libs.firebase.crashlytics.buildtools)

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // (Optional) Remove unless you're using media3
    // implementation(libs.androidx.media3.exoplayer.workmanager)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coroutines for async operations
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Existing dependencies (e.g., RecyclerView)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    implementation ("androidx.work:work-runtime-ktx:2.8.1") // For CoroutineWorker
    implementation ("com.google.code.gson:gson:2.10.1") // For Gson
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
