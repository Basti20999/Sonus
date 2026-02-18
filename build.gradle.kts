import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.pluginyml.bukkit) apply false
    alias(libs.plugins.gradleup.shadow) apply false
}

allprojects {
    group = "dev.minceraft.sonus"
    version = "2.0.0-SNAPSHOT"
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    repositories {
        maven("https://repo.minceraft.dev/public/")
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
            vendor = JvmVendorSpec.ADOPTIUM
        }
    }

    configure<PublishingExtension> {
        publications.create<MavenPublication>("maven") {
            artifactId = "${rootProject.name}-${project.name}".lowercase()
            from(components["java"])
        }
    }

    tasks {
        withType<JavaCompile> {
            options.release = 21
            options.encoding = Charsets.UTF_8.name()
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:removal")
            options.compilerArgs.add("-Xlint:deprecation")
        }

        withType<Jar> {
            archiveBaseName = "${rootProject.name}-${project.name}".lowercase()
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }

        withType<ShadowJar> {
            destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        }

        named("assemble") {
            findByName("shadowJar")?.apply {
                this@named.dependsOn(this@apply)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
