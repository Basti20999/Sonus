dependencies {
    api(projects.protocol)
    api(libs.speex4j)
    api(libs.opus4j)

    sequenceOf(
        libs.netty.transport.epoll,
        libs.netty.transport.iouring,
    ).forEach {
        implementation(it)
        runtimeOnly(variantOf(it) { classifier("linux-x86_64") })
        runtimeOnly(variantOf(it) { classifier("linux-aarch_64") })
    }
    sequenceOf(
        libs.netty.transport.kqueue,
    ).forEach {
        implementation(it)
        runtimeOnly(variantOf(it) { classifier("osx-x86_64") })
        runtimeOnly(variantOf(it) { classifier("osx-aarch_64") })
    }

    api(projects.svcAdapter)
}
