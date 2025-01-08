plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)

    //id ("dagger.hilt.android.plugin")
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.repoint.sources"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(project(":core:Dependencies"))
    implementation(project(":core:models"))
    implementation(project(":core:database"))

    implementation(libs.hilt.android)
    implementation(project(":core:basics"))

    kapt(libs.hilt.compiler)
    //kapt("com.google.dagger:hilt-android-compiler:2.51.1")
   // kapt ("com.google.dagger:hilt-compiler:2.51.1")
}