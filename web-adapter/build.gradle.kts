dependencies {
    api(projects.webProtocol)

    api(projects.webrtcPionIpc)
    runtimeOnly(projects.webrtcPion)

    compileOnly(libs.bundles.configurate)

    api(libs.netty.codec.http)

    implementation(projects.network)
}
