dependencies {
    api(projects.webProtocol)

    compileOnly(libs.bundles.configurate)

    api(libs.netty.codec.http)
    api(libs.jrtc)

    implementation(projects.network)
}
