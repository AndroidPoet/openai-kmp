@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
import io.github.androidpoet.openai.Configuration
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.publish)
}
kotlin {
    explicitApi()
    jvmToolchain(17)
    androidTarget { publishLibraryVariants("release") }
    jvm(); iosX64(); iosArm64(); iosSimulatorArm64(); macosX64(); macosArm64()
    tvosX64(); tvosArm64(); tvosSimulatorArm64(); watchosX64(); watchosArm64(); watchosSimulatorArm64()
    linuxX64(); mingwX64(); wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            api(project(":openai-core"))
            implementation(project(":openai-client"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
android { namespace = "io.github.androidpoet.openai.files"; compileSdk = Configuration.COMPILE_SDK; defaultConfig { minSdk = Configuration.MIN_SDK } }
mavenPublishing { coordinates(Configuration.GROUP, "openai-files", Configuration.VERSION) }
