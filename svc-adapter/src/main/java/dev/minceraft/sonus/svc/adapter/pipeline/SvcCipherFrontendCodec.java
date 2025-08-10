package dev.minceraft.sonus.svc.adapter.pipeline;


import dev.minceraft.sonus.common.protocol.util.ContextMap;
import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class SvcCipherFrontendCodec extends SvcUdpPipelineNode<ByteBuf, ByteBuf> {

    private final Map<UUID, SvcCipherCodec> ciphers = new HashMap<>();

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) throws Exception {
        SvcCipherCodec cipher = this.ciphers.get(ctxMap.<UUID>getUnchecked("playerId"));
        if (cipher != null) {
            cipher.encode(ctx, msg, out, ctxMap);
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) throws Exception {
        SvcCipherCodec cipher = this.ciphers.get(ctxMap.<UUID>getUnchecked("playerId"));
        if (cipher != null) {
            cipher.decode(ctx, msg, out, ctxMap);
        }
    }

    public void setPlayerCipher(UUID playerId, UUID secret) {
        this.ciphers.put(playerId, new SvcCipherCodec(secret));
    }

    public void removePlayer(UUID playerId) {
        this.ciphers.remove(playerId);
    }
}
