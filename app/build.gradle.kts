import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.pointchange.audio"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.pointchange.audio"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

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
            signingConfig = signingConfigs.getByName("release")
//            isMinifyEnabled = false
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
    implementation(libs.androidx.material3)
    implementation(libs.espresso.core)
    implementation(libs.androidx.compose.runtime)
//    implementation(libs.androidx.paging.common)
//    implementation(libs.androidx.paging.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.bundles.nav3)
    implementation(libs.bundles.glance)

    implementation(libs.kotlinx.serialization.core)
    implementation("org.videolan.android:libvlc-all:3.7.0")

    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")
    implementation("androidx.room:room-paging:${room_version}")

    val paging_version = "3.3.6"
    implementation("androidx.paging:paging-runtime:${paging_version}")
    implementation("androidx.paging:paging-common:${paging_version}")
    implementation("androidx.paging:paging-compose:${paging_version}")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(libs.reorderable)
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.datastore:datastore-preferences-core:1.2.1")
    val work_version = "2.11.2"
    implementation("androidx.work:work-runtime-ktx:${work_version}")
}