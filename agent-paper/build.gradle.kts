import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
        minecraftVersion("1.21.11")
    }

    withType<ShadowJar> {
        relocate("org.bstats", "dev.minceraft.sonus.agent.paper.bstats")
    }
}
