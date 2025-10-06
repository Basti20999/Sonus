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

    shadowJar {
        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName = rootProject.name
        archiveClassifier = "velocity"
    }

    assemble {
        dependsOn(shadowJar)
    }
}
