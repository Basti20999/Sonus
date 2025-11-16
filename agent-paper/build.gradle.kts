import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.run.paper)
}

dependencies {
    api(projects.protocol)
    compileOnly(libs.paper.api)
}

configure<BukkitPluginDescription> {
    name = "SonusAgent"
    authors = listOf("pianoman911", "booky10")
    website = "https://minceraft.dev/sonus"
    apiVersion = "1.13"
    main = "dev.minceraft.sonus.agent.paper.SonusAgentPlugin"
}

tasks {
    runServer {
        runDirectory = project.layout.projectDirectory.dir("run")

        minecraftVersion("1.21.8")
    }

    shadowJar {

        archiveBaseName = rootProject.name
        archiveClassifier = "paper"
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")

        relocate("org.bstats", "de.pianoman911.playerculling.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
