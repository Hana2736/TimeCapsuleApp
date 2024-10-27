plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "lol.hana.timecapsule"
    compileSdk = 35

    defaultConfig {
        applicationId = "lol.hana.timecapsule"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.commons.io)
    //implementation ("com.google.android.material:material:1.11.0-alpha01")
//implementation(libs.material3)
    //implementation("androidx.compose.material3:material3-window-size-class:1.3.0")
   // implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha02")
}