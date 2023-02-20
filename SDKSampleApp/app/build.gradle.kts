import org.gradle.api.JavaVersion.VERSION_11

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

val fourthlineSdkVersion: String by project

android {
    defaultConfig {
        applicationId = "com.fourthline.sdksample"
        minSdk = 23
        targetSdk = 33
        compileSdk = 33
        versionCode = 41
        versionName = fourthlineSdkVersion
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }

    kotlinOptions {
        jvmTarget = VERSION_11.toString()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    implementation("com.fourthline:fourthline-vision:$fourthlineSdkVersion")
// Uncomment this if you want to include bundled version of ML Kit.
// Note: Bundled variant of Text recognition model doesn't exist!
//    {
//        exclude(group = "com.google.android.gms", module = "play-services-mlkit-face-detection")
//        exclude(group = "com.google.android.gms", module = "play-services-mlkit-barcode-scanning")
//    }
//
//    implementation("com.google.mlkit:face-detection:16.1.2")
//    implementation("com.google.mlkit:barcode-scanning:17.0.0")

    implementation("com.fourthline:fourthline-nfc:$fourthlineSdkVersion")
    implementation("com.fourthline:fourthline-kyc:$fourthlineSdkVersion")
    implementation("com.fourthline:fourthline-sdk:$fourthlineSdkVersion")

// Uncomment this if you want to enable Analytics support
//    implementation("com.datadoghq:dd-sdk-android:1.10.0")
}