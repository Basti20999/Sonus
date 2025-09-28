package dev.minceraft.sonus.plasmo.protocol.tcp.data;


import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class EncryptionInfo {

    private final String algorithm;
    private final byte[] data;

    public EncryptionInfo(String algorithm, byte[] data) {
        this.algorithm = algorithm;
        this.data = data;
    }

    public EncryptionInfo(ByteBuf buf) {
        this.algorithm = Utf8String.readUnsignedShort(buf);
        this.data = DataTypeUtil.readIntFramedByteArray(buf);
    }

    public void write(ByteBuf buf) {
        Utf8String.writeUnsignedShort(buf, this.algorithm);
        DataTypeUtil.writeIntFramedByteArray(buf, this.data);
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "EncryptionInfo{" +
                "algorithm='" + algorithm + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
