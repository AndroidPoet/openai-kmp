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
}
android { namespace = "io.github.androidpoet.openai.models"; compileSdk = Configuration.COMPILE_SDK; defaultConfig { minSdk = Configuration.MIN_SDK } }
mavenPublishing { coordinates(Configuration.GROUP, "openai-models", Configuration.VERSION) }

