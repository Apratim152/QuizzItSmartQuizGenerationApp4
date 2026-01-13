// ---------- REQUIRED IMPORTS (Kotlin DSL) ----------
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.quizzit"
    compileSdk = 36

    // ---------- Load local.properties ----------
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.example.quizzit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ---------- Gemini API Key ----------
        buildConfigField(
            "String",
            "GEMINI_API_KEY1",
            "\"${localProperties.getProperty("GEMINI_API_KEY1", "")}\""
        )
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

    // ---------- Java / Kotlin ----------
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // ---------- Features ----------
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // ---------- Packaging Conflict Fixes ----------
    packaging {
        pickFirst("**/LICENSE")
        pickFirst("**/META-INF/DEPENDENCIES")
    }
}

// ---------- Dependency Resolution (BC Conflict Fix) ----------
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.bouncycastle") {
                useTarget(
                    "org.bouncycastle:bcprov-jdk18on:1.78"
                )
            }
        }
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }
}

dependencies {

    // ---------- Core Android ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ---------- Google Gemini AI ----------
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // ---------- HTML Parsing ----------
    implementation("org.jsoup:jsoup:1.17.2")

    // ---------- PDF ----------
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // ---------- DOC ----------
    implementation("org.apache.poi:poi:5.3.0")

    // ---------- Crypto ----------
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78")
    implementation(libs.mediation.test.suite)

    // ---------- Firebase ----------
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:17.1.1")

    // Firebase Coroutines support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    // ---------- Room ----------
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // ---------- Lifecycle ----------
    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // ---------- Coroutines ----------
    val coroutinesVersion = "1.9.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // ---------- UI ----------
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // ---------- Testing ----------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}