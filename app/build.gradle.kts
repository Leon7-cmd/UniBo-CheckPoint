import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.3.0"
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

val secretsPropertiesFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()
if (secretsPropertiesFile.exists()) {
    secretsProperties.load(FileInputStream(secretsPropertiesFile))
} else {
    logger.warn("️ ATTENZIONE: File screts.properties non trovato nella radice del progetto!")
}

android {
    namespace = "com.example.checkpoint"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.checkpoint"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Legge le proprietà o assegna stringa vuota se il file non esiste ancora
        val steamKey = secretsProperties.getProperty("STEAM_API_KEY") ?: ""
        val retroKey = secretsProperties.getProperty("RETRO_API_KEY") ?: ""
        val retroUser = secretsProperties.getProperty("RETRO_USERNAME") ?: ""
        val igdbId = secretsProperties.getProperty("IGDB_CLIENT_ID") ?: ""
        val igdbSecret = secretsProperties.getProperty("IGDB_CLIENT_SECRET") ?: ""

        // Generazione dei campi in BuildConfig
        buildConfigField("String", "STEAM_API_KEY", "\"$steamKey\"")
        buildConfigField("String", "RETRO_API_KEY", "\"$retroKey\"")
        buildConfigField("String", "RETRO_USERNAME", "\"$retroUser\"")
        buildConfigField("String", "IGDB_CLIENT_ID", "\"$igdbId\"")
        buildConfigField("String", "IGDB_CLIENT_SECRET", "\"$igdbSecret\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation("androidx.compose.material:material-icons-extended:1.7.x")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Image Loading & Networking
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Room Database
    val roomVersion = "2.7.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}