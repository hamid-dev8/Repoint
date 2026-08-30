plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")

    alias(libs.plugins.hilt)
}

android {
    namespace = "com.repoint.dashboard"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
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

    implementation(libs.hilt.android)
    implementation(project(":core:basics"))
    implementation(project(":core:models"))
    implementation(project(":core:network"))
    implementation(project(":feature:account"))
    
    //walletConnect
   implementation(platform(libs.reown.android.bom))
   implementation(libs.reown.android.core)
   implementation(libs.walletkit)
    //

   implementation(libs.play.services.code.scanner)
   // implementation(project(":feature:splash"))
   implementation(libs.androidx.runtime.livedata)
    implementation(project(":core:database"))
    implementation(project(":data:sources"))
    implementation(libs.androidx.media3.common.ktx)



    kapt (libs.hilt.compiler)

}

kapt{
    correctErrorTypes = true
}