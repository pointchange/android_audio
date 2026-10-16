import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.pointchange.sampleaudio"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    defaultConfig {
        applicationId = "com.pointchange.sampleaudio"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    val properties = Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        properties.load(FileInputStream(localFile))
    }
    signingConfigs {
        create("release") {
            val keyPath = properties.getProperty("KEY_PATH")
            storeFile = if (keyPath != null) file(keyPath) else null
            storePassword = properties.getProperty("KEY_PASSWORD")
            keyAlias = properties.getProperty("ALIAS_NAME")
            keyPassword = properties.getProperty("ALIAS_PASSWORD")
        }
    }
    buildTypes {
        release {
//            optimization {
//                enable = false
//            }
//            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.paging.common)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("androidx.media3:media3-common:1.10.1")
    implementation("androidx.media3:media3-ui-compose-material3:1.10.1")
    implementation("org.videolan.android:libvlc-all:3.7.5")
    implementation("androidx.media3:media3-session:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.11.0")
    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")
    implementation("androidx.room:room-paging:${room_version}")

    val paging_version = "3.4.2"

    implementation("androidx.paging:paging-common:$paging_version")

    // Jetpack Compose integration
    implementation("androidx.paging:paging-compose:$paging_version")
    val lifecycle_version = "2.10.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${lifecycle_version}")

    implementation(libs.bundles.serialization)
    implementation(libs.bundles.nav3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation(libs.reorderable)
//    implementation("io.github.rroohit:ImageCropView:4.1.0")

}