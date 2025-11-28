package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class WebsocketPacket extends AbstractWebPacket<IWebSocketHandler> {

    protected void failClientboundDecode() {
        throw new IllegalStateException("Tried decoding clientbound packet");
    }
}
