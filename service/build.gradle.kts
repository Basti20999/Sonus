dependencies {
    api(projects.protocol)
    api(libs.bundles.configurate)
    api(libs.bundles.netty.transport)

    compileOnly(projects.svcAdapter)
}
