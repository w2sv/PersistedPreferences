plugins {
    id("w2sv.android-library")
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
