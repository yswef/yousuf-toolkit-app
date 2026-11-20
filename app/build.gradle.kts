plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.yousuf.toolkit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yousuf.toolkit"
        minSdk = 34
        targetSdk = 36
        versionCode = 11
        versionName = "0.10.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val path = System.getenv("KEYSTORE_PATH")
            if (!path.isNullOrBlank()) {
                storeFile = file(path)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
        create("test") {
            storeFile = rootProject.file("ci/test-signing.p12")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeType = "PKCS12"
        }
    }

    buildTypes {
        release {
            val hasKey = !System.getenv("KEYSTORE_PATH").isNullOrBlank()
            signingConfig = if (hasKey) signingConfigs.getByName("release") else signingConfigs.getByName("test")
            isMinifyEnabled = false
        }
        create("qa") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("test")
            isDebuggable = true
        }
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("test")
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDir("../config")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.register("assembleTester") {
    dependsOn("assembleQa")
}
