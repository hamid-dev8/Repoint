
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    //hilt plugin
    alias(libs.plugins.hilt) apply false
    //id ("dagger.hilt.android.plugin") version "2.51.1" apply false

    //ksp
    alias(libs.plugins.ksp) apply false
    //kotlin("kapt") version "2.1.0"

    // kotlin("jvm") version "2.0.0" apply false
}
subprojects {
    // This block forces all subprojects to use the same, correct versions
    // of Kotlin and KSP, overriding any bad transitive dependencies.
    configurations.all {
        resolutionStrategy {
            eachDependency {
                // Force the Kotlin version
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion("2.1.0")
                    //reason = "Forcing all Kotlin libraries to 2.0.0"
                }

                // Force the KSP version
                if (requested.group == "com.google.devtools.ksp") {
                    useVersion("2.1.0-1.0.29")
                    //reason = "Forcing all KSP libraries to 2.0.0-1.0.21"
                }

                //for okhttp
                if (requested.group == "com.squareup.okhttp3") {
                    useVersion("4.12.0")
                }

                if (requested.group == "com.reown" ||
                    requested.name == "walletkit" ||
                    requested.name == "android-core") {
                    // Force 1.4.9 regardless of what version was requested
                    useVersion("1.4.8")
                }
            }
        }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // ✅ Correct syntax for Kotlin DSL
        classpath("org.web3j:web3j-gradle-plugin:4.8.0")
    }
}