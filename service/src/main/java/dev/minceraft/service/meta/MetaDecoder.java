package dev.minceraft.service.meta;
// Created by booky10 in Sonus (02:21 17.07.2025)

import dev.minceraft.service.SonusService;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MetaDecoder {

    private final MetaHandler handler;

    public MetaDecoder(SonusService service) {
        this.handler = new MetaHandler(service);
    }

    public void decode(byte[] array) {
        ByteBuf buf = Unpooled.wrappedBuffer(array);
        try {
            this.decode(buf);
        } finally {
            buf.release();
        }
    }

    public void decode(ByteBuf buf) {
        IMetaMessage message = MetaRegistry.REGISTRY.read(buf);
        message.handle(this.handler);
    }
}
