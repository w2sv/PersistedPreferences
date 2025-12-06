plugins {
    id("w2sv.jvm-kotlin-library")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            groupId = "com.w2sv.datastoreutils"
            artifactId = "datastoreflow"
            afterEvaluate {
                from(components["java"])
            }
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.w2sv.kotlinutils)
}
