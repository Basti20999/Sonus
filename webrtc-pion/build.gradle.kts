val compileOutputDir = project.layout.buildDirectory.dir("go")
val packOutputDir = project.layout.buildDirectory.dir("packed_go")
val compressionLevel = 9

val compileGo by tasks.registering {
    outputs.dir(compileOutputDir)
}
val packGo by tasks.registering {
    dependsOn(compileGo)
    inputs.dir(compileOutputDir)
    outputs.dir(packOutputDir)
}

sequenceOf("darwin", "windows", "linux").forEach { os ->
    sequenceOf("amd64", "arm64").forEach { arch ->
        val capOs = os.replaceFirstChar { it.titlecaseChar() }
        val capArch = arch.replaceFirstChar { it.titlecaseChar() }

        val fileName = "pion_${os}_$arch"
        val compileOutputFile = compileOutputDir.get().file(fileName)
        val packOutputFile = packOutputDir.get().file(fileName)

        val compileTask = tasks.register<Exec>("compileGo$capOs$capArch") {
            workingDir(project.layout.projectDirectory)

            inputs.files(
                project.fileTree(project.layout.projectDirectory)
                    .filter { it.name.endsWith(".go") }
            )
            inputs.files("go.mod", "go.sum")
            outputs.file(compileOutputFile)

            // activate cross-compilation
            environment("GOOS" to os, "GOARCH" to arch)
            commandLine(
                "go", "build", "-v",
                // strim useless metadata
                "-trimpath", "-ldflags", "-s -w",
                // specify output file location
                "-o", compileOutputFile, "main.go"
            )
        }

        // upx doesn't support packing windows arm64 exec files currently
        val packable = os != "windows" || arch != "arm64"

        val packTask = if (packable) {
            tasks.register<Exec>("packGo$capOs$capArch") {
                commandLine("upx", "-$compressionLevel", compileOutputFile, "-o$packOutputFile")
            }
        } else {
            tasks.register<Copy>("packGo$capOs$capArch") {
                from(compileOutputFile)
                into(packOutputDir)
                doLast {
                    logger.lifecycle("skipped packing, unsupported")
                }
            }
        }
        // apply common config
        packTask {
            dependsOn(compileTask)
            mustRunAfter(compileGo)

            inputs.files(compileOutputFile)
            outputs.files(packOutputFile)
        }

        compileGo {
            dependsOn(compileTask)
        }
        packGo {
            dependsOn(packTask)
        }
    }
}

tasks.named<Jar>("jar") {
    val activatePacking = rootProject.hasProperty("packGo")
    inputs.property("packGo", activatePacking)

    from(if (activatePacking) packGo else compileGo) {
        into("webrtc-pion")
    }
}
