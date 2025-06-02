plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.devtools.ksp")

    alias(libs.plugins.hilt)

}

android {
    namespace = "com.repoint.account"
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
    }
}

dependencies {

    implementation(project(":core:Dependencies"))
    implementation(project(":core:models"))
    implementation(project(":core:basics"))
    implementation(project(":core:database"))
    implementation(project(":data:sources"))


    implementation(libs.hilt.android)
    //implementation(project(":feature:splash"))

    //  kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    kapt (libs.hilt.compiler)
}
kapt{
    correctErrorTypes = true
}