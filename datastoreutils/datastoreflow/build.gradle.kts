plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            groupId = "com.w2sv.datastoreutils"
            artifactId = "datastoreflow"
            version = version.toString()
            afterEvaluate {
                from(components["java"])
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
    implementation(libs.kotlinx.coroutines)
    implementation(libs.w2sv.kotlinutils)
}
