dependencies {
    api(projects.webProtocol)

    compileOnly(libs.bundles.configurate)

    api(libs.netty.codec.http)

    api("org.freedesktop.gstreamer:gst1-java-core:1.4.0")

    implementation(projects.network)
}
