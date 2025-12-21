import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.run.velocity)
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)

    compileOnly(projects.service)
    runtimeOnly(projects.service) {
        targetConfiguration = "shadow"
    }
}

tasks {
    runVelocity {
        runDirectory = project.layout.projectDirectory.dir("run")
        velocityVersion("3.4.0-SNAPSHOT")
    }

    withType<ShadowJar> {
        // velocity already includes netty dependencies, so exclude them
        exclude("io/netty/**")
    }
}
