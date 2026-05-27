import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "com.w2sv.${path.removePrefix(":").replace(':', '.').replace('-', '.')}"
    compileSdk = 37

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes { getByName("release") { isMinifyEnabled = false } }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { buildConfig = false }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { test -> test.failOnNoDiscoveredTests = false }
        }
    }

    // unit test tasks for some reason need minSdk=23
    sourceSets {
        getByName("test") {
            defaultConfig {
                minSdk = 23
            }
        }
    }
}
