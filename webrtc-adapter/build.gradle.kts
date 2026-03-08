dependencies {
    compileOnlyApi(libs.jetbrains.annotations)
    compileOnlyApi(libs.slf4j.api)
    api(projects.network)
    api(projects.webrtcIpc)
}
