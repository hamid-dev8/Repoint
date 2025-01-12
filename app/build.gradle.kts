plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    //hilt need
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    alias(libs.plugins.hilt)
   // id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.repoint.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.repoint.app"
        minSdk = 26
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
        compose = true
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    implementation(project(":core:Dependencies"))
    implementation(project(":core:basics"))
    implementation(project(":feature"))
    implementation(project(":feature:account"))
    implementation(project(":feature:dashboard"))
    implementation(project(":core:models"))

    implementation(libs.hilt.android)

    //kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    kapt(libs.hilt.compiler)
}


hilt{
    enableAggregatingTask = true
}


kapt {
    correctErrorTypes = true
 /*   javacOptions {
        // These options are normally set automatically via the Hilt Gradle plugin, but we
        // set them manually to workaround a bug in the Kotlin 1.5.20
        option("-Adagger.fastInit=ENABLED")
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }*/
}