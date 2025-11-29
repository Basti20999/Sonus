package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class WebSocketPacket extends AbstractWebPacket<IWebSocketHandler> {
}
