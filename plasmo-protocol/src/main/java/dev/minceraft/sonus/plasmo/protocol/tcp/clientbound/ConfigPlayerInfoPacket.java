package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;


import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConfigPlayerInfoPacket<T extends ConfigPlayerInfoPacket<T>> extends TcpPlasmoPacket<T> {

    protected @MonotonicNonNull Object2BooleanMap<String> permissions;

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeMap(buf, this.permissions, Utf8String::writeUnsignedShort, ByteBuf::writeBoolean);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.permissions = DataTypeUtil.readMap(buf, Utf8String::readUnsignedShort, ByteBuf::readBoolean, Object2BooleanArrayMap::new);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleConfigPlayerInfoPacket(this);
    }

    public Object2BooleanMap<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Object2BooleanMap<String> permissions) {
        this.permissions = permissions;
    }
}
