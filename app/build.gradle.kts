plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}


val gitDescribe = providers.exec {
    commandLine("git", "describe", "--tags", "--always")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }.orElse("1.0")

val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().toInt() }.orElse(1)


android {
    namespace = "com.eclipse.music.kit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.eclipse.music.kit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionCode = 1 + gitCommitCount.get()
        versionName = "1.0" + gitDescribe.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module"
            )
        }
    }

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x64")
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_19
            freeCompilerArgs = listOf(
                "-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions"
            )
        }
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.sceneform.rendering)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kyant0.taglib)
}
