import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.run.velocity)
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
    implementation(projects.service)
}

tasks {
    runVelocity {
        runDirectory = project.layout.projectDirectory.dir("run")
        velocityVersion("3.4.0-SNAPSHOT")
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
