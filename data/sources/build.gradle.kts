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
        buildConfigField("String", "WALLETCONNECT_PROJECT_ID", "\"ae6a1b168640a2f79f8e008027614ce3\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                file("'../core/Dependencies/proguard-rules.pro")

            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // ✅ Exclude conflicting META-INF files
    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
    }
}

dependencies {

    implementation(project(":core:Dependencies"))
    implementation(project(":core:models"))
    implementation(project(":core:database"))
    implementation(project(":core:basics"))

    implementation(libs.hilt.android)
    implementation(project(":core:network"))

    kapt(libs.hilt.compiler)
    //kapt("com.google.dagger:hilt-android-compiler:2.51.1")
   // kapt ("com.google.dagger:hilt-compiler:2.51.1")

    //walletConnect
    implementation(platform(libs.reown.android.bom))
    implementation(libs.reown.android.core)
    implementation(libs.walletkit)
}