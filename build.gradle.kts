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
    kotlin("kapt") version "2.1.0"

    // kotlin("jvm") version "2.0.0" apply false
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