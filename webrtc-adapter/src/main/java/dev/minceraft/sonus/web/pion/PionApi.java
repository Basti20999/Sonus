package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (3:23 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcConnection;
import dev.minceraft.sonus.web.pion.ipc.model.BundlePolicy;
import dev.minceraft.sonus.web.pion.ipc.model.IceServer;
import dev.minceraft.sonus.web.pion.ipc.model.IceTransportPolicy;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.List;

@NullMarked
public final class PionApi implements AutoCloseable {

    private static final String SOCKET_PATH_ENV = "PION_IPC_SOCKET";
    private static final Path DEFAULT_SOCKET_PATH = Path.of(System.getProperty(SOCKET_PATH_ENV, "/tmp/pion.socket"));

    private final Process process;
    final IpcConnection ipc;

    @ApiStatus.Internal
    public PionApi(Process process, Path socketPath) {
        this.process = process;
        this.ipc = IpcConnection.connect(socketPath);
    }

    public PionPeer allocatePeer(
            List<IceServer> iceServers, IceTransportPolicy iceTransportPolicy,
            BundlePolicy bundlePolicy, String id, PionPeer.Callback callback
    ) {
        return new PionPeer(this.ipc, callback, iceServers, iceTransportPolicy, bundlePolicy, id);
    }

    @Override
    public void close() {
        try {
            this.ipc.close();
        } finally {
            if (this.process.isAlive()) {
                this.process.destroy();
            }
        }
    }
}
