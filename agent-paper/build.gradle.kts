import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.pluginyml.bukkit)
}

dependencies {
    api(projects.protocol)
    compileOnly(libs.paper.api)
}

configure<BukkitPluginDescription> {
    authors = listOf("pianoman911", "booky10")
    website = "https://minceraft.dev/sonus"
    apiVersion = "1.21.4"
    main = "dev.minceraft.sonus.agent.paper.SonusAgentPlugin"
}
