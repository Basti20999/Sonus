val compileOutputDir = project.layout.buildDirectory.dir("go")

val compileGo by tasks.registering {
    outputs.dir(compileOutputDir)
}

sequenceOf("darwin", "windows", "linux").forEach { os ->
    sequenceOf("amd64", "arm64").forEach { arch ->
        val capOs = os.replaceFirstChar { it.titlecaseChar() }
        val capArch = arch.replaceFirstChar { it.titlecaseChar() }

        val compileTask = tasks.register<Exec>("compileGo$capOs$capArch") {
            workingDir(project.layout.projectDirectory)

            inputs.files(
                project.fileTree(project.layout.projectDirectory)
                    .filter { it.name.endsWith(".go") }
            )
            inputs.files("go.mod", "go.sum")

            val outputFile = compileOutputDir.get().file("pion_${os}_$arch")
            outputs.file(outputFile)

            // activate cross-compilation
            environment("GOOS" to os, "GOARCH" to arch)
            commandLine(
                "go", "build", "-v",
                // strim useless metadata
                "-trimpath", "-ldflags", "-s -w",
                // specify output file location
                "-o", outputFile, "main.go"
            )
        }

        compileGo {
            dependsOn(compileTask)
        }
    }
}

tasks.named<Jar>("jar") {
    from(compileGo) {
        into("webrtc-pion")
    }
}
