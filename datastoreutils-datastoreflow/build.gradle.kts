plugins {
    id("w2sv.jvm-kotlin-library")
    id("w2sv.maven-publish-convention")
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.w2sv.kotlinutils)
}
