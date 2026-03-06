dependencies {
    api(projects.webProtocol)
    api(projects.webPion)

    compileOnly(libs.bundles.configurate)

    api(libs.netty.codec.http)

    implementation(projects.network)
}
