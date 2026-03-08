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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
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

        String execName = "webrtc-pion/pion_" + operatingSystem + "_" + systemArch;
        try {
            Path tempPath = Files.createTempFile("webrtcpion", "bin");
            // delete on exit using old file api
            tempPath.toFile().deleteOnExit();

            // lookup executable in classpath
            try (InputStream execStream = PionLauncher.class.getClassLoader().getResourceAsStream(execName)) {
                if (execStream == null) {
                    throw new IllegalStateException("Can't find binary in classpath: " + execName);
                }
                Files.copy(execStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // non-windows requires us to mark the executable as executable
            // needs to be done after copying exec from classpath because our copy overwrites the previous file
            if (operatingSystem != OperatingSystem.WINDOWS) {
                Files.setPosixFilePermissions(tempPath, PosixFilePermissions.fromString("rwxr--r--")); // 0o744
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
                ProcessBuilder bob = new ProcessBuilder(execPath.toString(), socketPath.toAbsolutePath().toString());
                // redir stderr to stdout
                bob.redirectErrorStream(true);
                // mark as embedded
                bob.environment().put("PION_EMBEDDED", "1");
                process = bob.start();
            } catch (IOException exception) {
                throw new RuntimeException("Error while starting process for " + socketPath, exception);
            }
            // launch logger thread, automatically exits when process is dead
            PionLogTask.launch(process, "PionProcess");
            // wait for process to start up
            return CompletableFuture.runAsync(() -> {
                        // wait for socket path to be allocated
                        long timeout = System.nanoTime() + 5 * 1000 * 1_000_000L; // timeout after 5s
                        while (Files.notExists(socketPath)) {
                            if (!process.isAlive()) {
                                throw new IllegalStateException("Pion process exited: " + process.exitValue());
                            } else if (System.nanoTime() > timeout) {
                                throw new IllegalStateException("Timed out while waiting for pion process to start");
                            }
                            LockSupport.parkNanos(10 * 1_000_000L); // 10ms
                        }
                    })
                    .thenApply(__ -> new PionApi(process, socketPath))
                    .whenComplete((result, error) -> {
                        if (error != null && process.isAlive()) {
                            // stop process on unexpected error
                            process.destroy();
                        }
                    });
        });
    }
}
