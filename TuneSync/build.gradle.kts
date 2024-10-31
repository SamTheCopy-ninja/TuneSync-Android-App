// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false

    // Added plugin for KSP
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
}

    // Added buildscript for passing safe arguments for navigation
buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.0")
    }
}