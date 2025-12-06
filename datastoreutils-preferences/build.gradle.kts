plugins {
    id("w2sv.android-library")
    id("w2sv.maven-publish-convention")
}

dependencies {
    api(projects.datastoreutilsDatastoreflow)
    api(libs.androidx.datastore.preferences)
    implementation(libs.slimber)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.kotlinx.coroutines.android)
}
