plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "il.org.iluy.zmanim"
    compileSdk = 34

    defaultConfig {
        applicationId = "il.org.iluy.zmanim"
        // Android 8.1 (Oreo MR1) = API 27. Watch reports "Android 8.1".
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-poc"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Offline halachic zmanim calculation library (no network calls).
    // Verify the latest version on Maven Central before building:
    // https://mvnrepository.com/artifact/com.kosherjava/zmanim
    implementation("com.kosherjava:zmanim:2.5.0")
}
