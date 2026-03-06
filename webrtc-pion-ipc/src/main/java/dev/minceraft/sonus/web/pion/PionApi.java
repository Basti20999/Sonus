package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (3:23 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

@NullMarked
public final class PionApi implements AutoCloseable {

    private static final Path DEFAULT_SOCKET_PATH = Path.of(System.getProperty("PION_IPC_SOCKET", "/tmp/pion.socket"));

    final IpcConnection ipc;

    public PionApi() {
        this(DEFAULT_SOCKET_PATH);
    }

    public PionApi(Path socketPath) {
        this.ipc = IpcConnection.connect(socketPath);
    }

    public PionPeer allocatePeer(
            List<IceServer> iceServers, BundlePolicy bundlePolicy, String id, PionPeer.Callback callback
    ) {
        return new PionPeer(this.ipc, callback, iceServers, bundlePolicy, id);
    }

    @Override
    public void close() {
        this.ipc.close();
    }

    public enum BundlePolicy {
        UNKNOWN,
        BALANCED,
        MAX_COMPAT,
        MAX_BUNDLE,
    }

    public record IceServer(
            String url,
            @Nullable String user,
            @Nullable String auth
    ) {
    }
}
