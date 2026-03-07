package dev.minceraft.sonus.web.pion.launcher;
// Created by booky10 in Sonus (3:09 AM 07.03.2026)

import dev.minceraft.sonus.web.pion.PionApi;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

@NullMarked
public final class PionLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger("PionLauncher");

    private PionLauncher() {
    }

    private static Path extractBinary() {
        OperatingSystem operatingSystem = OperatingSystem.detect();
        SystemArch systemArch = SystemArch.detect();
        if (operatingSystem == OperatingSystem.UNSUPPORTED || systemArch == SystemArch.UNSUPPORTED) {
            throw new IllegalStateException("Detected unsupported operating system (" + operatingSystem + ") or architecture (" + systemArch + ")");
        }
        LOGGER.info("Will be extracting pion (webrtc) for {}/{}...", operatingSystem, systemArch);

        String execName = "/webrtc-pion/pion_" + operatingSystem + "_" + systemArch;
        try {
            Path tempPath = Files.createTempFile("webrtcpion", "bin");
            // delete on exit using old file api
            tempPath.toFile().deleteOnExit();

            // lookup executable in classpath
            try (InputStream execStream = PionLauncher.class.getClassLoader().getResourceAsStream(execName)) {
                if (execStream == null) {
                    throw new IllegalStateException("Can't find binary in classpath: " + execName);
                }
                Files.copy(execStream, tempPath);
            }
            return tempPath.toAbsolutePath();
        } catch (IOException exception) {
            throw new IllegalStateException("Error while extracting binary from classpath: " + exception, exception);
        }
    }

    public static CompletableFuture<PionApi> launch() {
        Path socketPath;
        try {
            socketPath = Files.createTempDirectory("pionsocket").resolve("pion.socket");
        } catch (IOException exception) {
            throw new RuntimeException("Failed to create temporary directory for pion ipc socket", exception);
        }
        return launch(socketPath);
    }

    public static CompletableFuture<PionApi> launch(Path socketPath) {
        if (Files.exists(socketPath)) {
            throw new IllegalStateException("Can't create socket at " + socketPath + ", something already exists there");
        }
        return CompletableFuture.supplyAsync(PionLauncher::extractBinary).thenCompose(execPath -> {
            LOGGER.info("Launching pion (webrtc) from {}...", execPath);
            Process process;
            try {
                process = new ProcessBuilder(execPath.toString(), socketPath.toAbsolutePath().toString())
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start();
            } catch (IOException exception) {
                throw new RuntimeException("Error while starting process for " + socketPath, exception);
            }
            // wait for process to start up
            return CompletableFuture.runAsync(() -> {
                // wait for socket path to be allocated
                while (Files.notExists(socketPath)) {
                    if (!process.isAlive()) {
                        throw new IllegalStateException("Pion process exited: " + process.exitValue());
                    }
                    LockSupport.parkNanos(10 * 1_000_000L); // 10ms
                }
            }).thenApply(__ -> new PionApi(process, socketPath));
        });
    }
}
