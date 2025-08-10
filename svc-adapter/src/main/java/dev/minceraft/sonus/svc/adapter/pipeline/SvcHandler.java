package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineHandler;
import dev.minceraft.sonus.common.protocol.util.ContextMap;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

public class SvcHandler extends AbstractUdpPipelineHandler<SvcVoicePacket<?>> {

    private final ISonusService service;

    public SvcHandler(ISonusService service) {
        super(SvcUdpMagicCodec.INSTANCE);
        this.service = service;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, SvcVoicePacket<?> msg, ContextMap ctxMap) {
        UUID playerId = ctxMap.getUnchecked("playerId");
        ISonusPlayer player = this.service.getPlayer(playerId);
        if (player == null) {
            ctx.close();
            return;
        }
        // TODO: per-player handler
    }
}
