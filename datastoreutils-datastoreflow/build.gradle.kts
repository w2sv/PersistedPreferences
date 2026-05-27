plugins {
    id("w2sv.jvm-kotlin-library")
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.w2sv.kotlinutils.coroutines)
}
