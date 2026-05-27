plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    // Use module name as artifactId
    coordinates(
        artifactId = project.name,
        version = rootProject.version.toString()
    )
}
