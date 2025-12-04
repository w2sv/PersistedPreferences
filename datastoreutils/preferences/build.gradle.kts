plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

kotlin {
    jvmToolchain(11)
}

android {
    namespace = "com.w2sv.datastoreutils.preferences"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    @Suppress("UnstableApiUsage")
//    testOptions {
//        unitTests {
//            isIncludeAndroidResources = true
//            isReturnDefaultValues = true
//        }
//    }
    buildFeatures {
        buildConfig = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.w2sv.datastoreutils"
            artifactId = "preferences"
            version = version.toString()
            afterEvaluate {
                from(components["release"])
            }
            pom {
                developers {
                    developer {
                        id.set("w2sv")
                        name.set("Janek Zangenberg")
                    }
                }
                description.set("Utilities for working with the androidx DataStore.")
                url.set("https://github.com/w2sv/DataStoreUtils")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}

dependencies {
    api(project(":datastoreutils:datastoreflow"))
    api(libs.androidx.datastore.preferences)
    implementation(libs.slimber)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.kotlinx.coroutines.android)
//    testImplementation(libs.junit)
}
