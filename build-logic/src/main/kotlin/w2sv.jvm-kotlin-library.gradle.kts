plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    `maven-publish`
}

kotlin {
    jvmToolchain(Versions.kotlinJvmToolchain)
}
