plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.compose)

    //hilt need
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    id("kotlin-kapt") apply false
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
    testImplementation("junit:junit:4.12")
    androidTestApi(libs.androidx.junit)
    androidTestApi(libs.androidx.espresso.core)
    androidTestApi(platform(libs.androidx.compose.bom))
    androidTestApi(libs.androidx.ui.test.junit4)
    debugApi(libs.androidx.ui.tooling)
    debugApi(libs.androidx.ui.test.manifest)

    //navigation_compose
    api(libs.navigation.compose)

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


    //web3 android
    //noinspection GradleDependency
    api (libs.core.v489android)

    //hilt
    api(libs.hilt.android)

   // ksp(libs.hilt.compiler)

}
/*
kapt {
    correctErrorTypes = true
}*/
