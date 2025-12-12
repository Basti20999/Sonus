import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.gradleup.shadow)
}

dependencies {
    api(projects.protocol)
    api(libs.speex4j)

    // included in all service implementations
    compileOnlyApi(libs.adventure.text.logger.slf4j)

    implementation(projects.network)
    implementation(projects.svcAdapter)
    implementation(projects.plasmoAdapter)
    implementation(projects.webAdapter)
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    sequenceOf(
        // can't relocate these two dependencies, otherwise native linking would break
        // "de.maxhenkel.speex4j" to "speex4j",
        // "de.maxhenkel.opus4j" to "opus4j",
        "de.maxhenkel.nativeutils" to "nativeutils",
    ).forEach { (k, v) ->
        relocate(k, "${project.group}.libs.$v")
    }
}
