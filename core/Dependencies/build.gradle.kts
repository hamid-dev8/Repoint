plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.compose)

    //hilt need
    alias(libs.plugins.ksp) apply false

    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    //id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.repoint.dependencies"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }


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

    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.activity.compose)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.material3)
    testApi(libs.junit.v412)
    androidTestApi(libs.androidx.junit)
    androidTestApi(libs.androidx.espresso.core)
    androidTestApi(platform(libs.androidx.compose.bom))
    androidTestApi(libs.androidx.ui.test.junit4)
    debugApi(libs.androidx.ui.tooling)
    debugApi(libs.androidx.ui.test.manifest)

    //navigation_compose
    api(libs.navigation.compose)
    //compose constraint
    api(libs.androidx.constraintlayout.compose)

    // ViewModel
    api(libs.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    api(libs.androidx.lifecycle.viewmodel.compose)
    // LiveData
    api(libs.androidx.lifecycle.livedata.ktx)
    //kotlin coroutines
    api(libs.kotlinx.coroutines.android)

    //web3j
    //java//implementation (libs.core)

    api("androidx.browser:browser:1.8.0")  // or latest stable version

    //web3 android
    //noinspection GradleDependency
    api(libs.core.v489android)
    api(libs.codegen.v487)
    api( libs.utils)
    api(libs.crypto)
    api(libs.rlp)

    //hilt
    implementation(libs.hilt.android)
    api(libs.androidx.hilt.navigation.fragment)
    // Hilt core library
    api("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ksp(libs.hilt.compiler)
    kapt(libs.hilt.compiler)
    // Hilt compiler for annotation processing
    //kapt ("com.google.dagger:hilt-compiler:2.51.1")

    // Java language implementation
    api(libs.androidx.biometric)
    // Kotlin
    api(libs.androidx.biometric.v140alpha02)


    //gson
    api(libs.gson)

    //carousel & material3
    api(libs.material3)
    api(libs.androidx.material3.adaptive)
    api(libs.androidx.material3.v140alpha13)
    api(libs.androidx.material.ripple)


    // Jetpack Compose integration
    api(libs.navigation.compose)
    api(libs.androidx.animation)

    //api(libs.hilt.compiler)

    api(libs.androidx.appcompat.v170)
    // For loading and tinting drawables on older versions of the platform
    api(libs.androidx.appcompat.resources)

    api(libs.androidx.foundation)

    //extended icons material
    api(libs.androidx.material.icons.extended)

    //splash
    api(libs.androidx.core.splashscreen)

    api(libs.androidx.datastore.preferences)

    //qr
    api(libs.barcode.scanning)
    api(libs.play.services.code.scanner)
    api(libs.androidx.camera.core)
    api(libs.androidx.camera.camera2)
    api(libs.androidx.camera.lifecycle)
    api(libs.androidx.camera.view)

    //leak cannery
    debugApi(libs.leakcanary.android)
}


// Allow references to generated code
kapt {
    correctErrorTypes = true

    /*javacOptions {
        // These options are normally set automatically via the Hilt Gradle plugin, but we
        // set them manually to workaround a bug in the Kotlin 1.5.20
        option("-Adagger.fastInit=ENABLED")
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }*/
}
