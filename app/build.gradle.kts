import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    id("com.apollographql.android")
    id("com.diffplug.gradle.spotless") version "3.16.0"

    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "me.a0xcaff.forte"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    dataBinding {
        isEnabled = true
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

spotless {
    kotlin {
        ktlint()
        target("**/*.kt")
    }

    xml {
        target("**/*.xml")
    }
}

dependencies {
    val lifecycleVersion = "2.0.0"

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(kotlinx("coroutines-android", "1.1.0"))

    implementation("androidx.appcompat:appcompat:1.1.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-alpha3")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")

    val navVersion = "1.0.0-alpha11"
    implementation("android.arch.navigation:navigation-fragment-ktx:$navVersion")
    implementation("android.arch.navigation:navigation-ui-ktx:$navVersion")

    koinModules(
        listOf("core", "android", "androidx-scope", "androidx-viewmodel"),
        "1.0.2",
        this::implementation
    )

    exoplayerModules(
        listOf("exoplayer-core", "exoplayer-ui", "extension-mediasession", "extension-okhttp"),
        "2.9.3",
        this::implementation
    )

    implementation("com.facebook.stetho:stetho:1.5.0")
    implementation("com.facebook.stetho:stetho-okhttp3:1.5.0")

    implementation("com.google.android.material:material:1.1.0-alpha02")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.apollographql.apollo:apollo-runtime:1.0.0-alpha4")

    testImplementation("junit:junit:4.12")
    testImplementation("androidx.arch.core:core-testing:$lifecycleVersion")

    kapt("com.android.databinding:compiler:3.1.4")
}

fun koin(module: String, version: String): String =
    "org.koin:koin-$module:$version"

fun koinModules(modules: List<String>, version: String, register: (dependencyNotation: Any) -> Dependency?) =
    modules.forEach { register(koin(it, version)) }

fun exoplayer(module: String, version: String): String =
    "com.google.android.exoplayer:$module:$version"

fun exoplayerModules(modules: List<String>, version: String, register: (dependencyNotation: Any) -> Dependency?) =
    modules.forEach { register(exoplayer(it, version)) }

fun kotlinx(module: String, version: String): String =
    "org.jetbrains.kotlinx:kotlinx-$module:$version"
