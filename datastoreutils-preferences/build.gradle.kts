plugins {
    id("w2sv.android-library")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.w2sv.datastoreutils"
            artifactId = "preferences"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    api(projects.datastoreutilsDatastoreflow)
    api(libs.androidx.datastore.preferences)
    implementation(libs.slimber)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.kotlinx.coroutines.android)
}
