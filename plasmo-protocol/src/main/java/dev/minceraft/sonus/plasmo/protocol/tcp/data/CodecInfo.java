package dev.minceraft.sonus.plasmo.protocol.tcp.data;


import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class CodecInfo {

    private final String name;
    private final Map<String, String> params;

    public CodecInfo(ByteBuf buf) {
        this.name = Utf8String.readUnsignedShort(buf);
        this.params = DataTypeUtil.readMap(buf, Utf8String::readUnsignedShort, Utf8String::readUnsignedShort);
    }

    public CodecInfo(String name, Map<String, String> params) {
        this.name = name;
        this.params = params;
    }

    public void write(ByteBuf buf) {
        Utf8String.writeUnsignedShort(buf, this.name);
        DataTypeUtil.writeMap(buf, this.params, Utf8String::writeUnsignedShort, Utf8String::writeUnsignedShort);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    @Override
    public String toString() {
        return "CodecInfo{" +
                "name='" + name + '\'' +
                ", params=" + params +
                '}';
    }
}