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

    signingConfigs {
        create("release") {
            storeFile = file("repoint_key.jks")
            storePassword = "artaan"
            keyAlias = "repoint"
            keyPassword = "artaan"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
               file("../core/Dependencies/proguard-rules.pro"),
                file("../core/basics/proguard-rules.pro"),
                file("../core/database/proguard-rules.pro"),
                file("../core/models/proguard-rules.pro"),
                file("../core/models/proguard-rules.pro"),
                file("../core/network/proguard-rules.pro"),
                file("../data/sources/proguard-rules.pro"),
                file("../feature/proguard-rules.pro"),
                file("../feature/account/proguard-rules.pro"),
                file("../feature/dashboard/proguard-rules.pro"),
                file("../feature/splash/proguard-rules.pro")
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    implementation(project(":core:Dependencies"))
    implementation(project(":core:basics"))
    implementation(project(":core:models"))
    implementation(project(":feature"))
    implementation(project(":feature:account"))
    implementation(project(":feature:dashboard"))
    implementation(project(":data:sources"))
    implementation(project(":feature:splash"))

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