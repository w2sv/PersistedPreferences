plugins {
    id("w2sv.android-library")
}

dependencies {
    api(projects.datastoreutilsDatastoreflow)
    api(libs.androidx.datastore.preferences)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.kotlinx.coroutines.android)
}
