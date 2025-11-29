dependencies {
    api(projects.webProtocol)

    compileOnly(libs.bundles.configurate)

    api(libs.netty.codec.http)

    implementation(projects.network)
}