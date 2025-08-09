package de.pianoman911.sonus.svcprotocol.voice;


import de.pianoman911.sonus.svcprotocol.AbstractSvcPacket;
import de.pianoman911.sonus.svcprotocol.SvcUdpMagicCodec;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;

import java.net.InetSocketAddress;

public abstract class SvcVoicePacket<T extends SvcVoicePacket<?>> extends AbstractSvcPacket implements IUdpPacket {

    protected InetSocketAddress remoteAddress;

    protected SvcVoicePacket() {
    }

    public abstract void handle(ISonusPlayer player, IVoiceSvcHandler handler);

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public AbstractMagicUdpCodec<?> getCodec() {
        return SvcUdpMagicCodec.INSTANCE;
    }
}
