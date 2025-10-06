package dev.minceraft.sonus.plasmo.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import io.netty.util.Recycler;

import java.net.InetSocketAddress;

public class PlasmoUdpContext extends UdpBasedContext<PlasmoUdpContext> {

    public static final Recycler<PlasmoUdpContext> RECYCLER = createRecycler(PlasmoUdpContext::new);

    public PlasmoConnection connection;

    private PlasmoUdpContext(Recycler.Handle<PlasmoUdpContext> handle) {
        super(handle);
    }

    public static PlasmoUdpContext newInstance() {
        return RECYCLER.get();
    }

    public static PlasmoUdpContext newInstance(InetSocketAddress remoteAddress, PlasmoConnection connection) {
        PlasmoUdpContext plasmoUdpContext = newInstance();
        plasmoUdpContext.remoteAddress = remoteAddress;
        plasmoUdpContext.connection = connection;
        return plasmoUdpContext;
    }

    @Override
    protected void clear() {
        super.clear();
        this.connection = null;
    }
}
