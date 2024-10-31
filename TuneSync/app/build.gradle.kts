plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    // Added KSP and safe navigation arguments
    id("com.google.devtools.ksp")

    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.example.tunesync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tunesync"
        minSdk = 32
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Enable debugging for development
            isDebuggable = true
        }
        release {
            // Disable debugging for release builds
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.filament.android)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.rules)
    implementation(libs.core)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.database)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Unit testing
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)


    // Fragment and Navigation Component
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // TabLayout and ViewPager2
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Play audio in background
    implementation("androidx.media3:media3-session:1.4.1")

    // API operations
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // chart library
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Timeline library
    implementation ("com.github.vipulasri:Timeline-View:v1.0.6")

    // Biometrics for fingerprint
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.core:core-ktx:1.13.1")

}