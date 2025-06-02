plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    kotlin("kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)

}

android {
    namespace = "com.repoint.network"
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
                "proguard-rules.pro",
                file("'../core/Dependencies/proguard-rules.pro")

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

    //retrofit
    implementation(libs.retrofit)
    //gson
    implementation(libs.converter.gson)

    //kotlin coroutine adapter for retrofit
    //in retrofit 2.6 and higher it doesn't need to use this dependency
    //implementation(libs.adapter.kotlin.coroutines)



    //okhttp
    // define a BOM and its version
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //logging ihsanbal
    implementation("com.github.ihsanbal:LoggingInterceptor:3.1.0") {
        exclude(group = "org.json", module = "json")
    }

    //moralis
    //implementation(libs.web3.api.client)
    implementation(project(":core:models"))




    implementation(libs.hilt.android)
    implementation(project(":core:basics"))
    //  kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    kapt (libs.hilt.compiler)

}

// For KSP
ksp {
    arg("option_name", "option_value")
// other options...
}
kapt{
    correctErrorTypes = true
}