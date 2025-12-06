plugins {
    id("w2sv.android-library")
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
