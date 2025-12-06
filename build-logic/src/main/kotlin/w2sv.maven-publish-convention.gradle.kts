plugins {
    `maven-publish`
}

afterEvaluate {
    // Derive component name from applied kotlin plugin
    val componentName = when {
        pluginManager.hasPlugin("org.jetbrains.kotlin.android") -> "release"
        pluginManager.hasPlugin("org.jetbrains.kotlin.jvm") -> "java"
        else -> throw GradleException("Cannot derive what componentName to use")
    }

    publishing {
        publications {
            register<MavenPublication>(componentName) {
                groupId = "com.w2sv.${rootProject.name}" // Assumes rootProject.name to be set in root/settings.gradle.kts
                artifactId = project.name.substringAfterLast("-") // Assumes module name of 'project-artifact'
                from(components[componentName])
            }
        }
    }
}
