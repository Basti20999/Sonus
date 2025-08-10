package dev.minceraft.sonus.protocol.svc.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.protocol.svc.data.Codec;
import dev.minceraft.sonus.protocol.svc.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class SecretSvcPacket extends SvcMetaPacket<SecretSvcPacket> implements ClientBound {

    private @MonotonicNonNull UUID secret;
    private int serverPort;
    private @MonotonicNonNull UUID playerId;
    private @MonotonicNonNull Codec codec;
    private int mtuSize;
    private double voiceChatDistance;
    private int keepAlive;
    private boolean groupsEnabled;
    private @MonotonicNonNull String voiceHost;
    private boolean allowRecording;

    public SecretSvcPacket() {
        super(SvcPluginChannels.SECRET);
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.secret);
        buf.writeInt(this.serverPort);
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        buf.writeByte(this.codec.ordinal());
        buf.writeInt(this.mtuSize);
        buf.writeDouble(this.voiceChatDistance);
        buf.writeInt(this.keepAlive);
        buf.writeBoolean(this.groupsEnabled);
        Utf8String.write(buf, this.voiceHost);
        buf.writeBoolean(this.allowRecording);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.secret = DataTypeUtil.readUniqueId(buf);
        this.serverPort = buf.readInt();
        this.playerId = DataTypeUtil.readUniqueId(buf);
        this.codec = Codec.values()[buf.readByte()];
        this.mtuSize = buf.readInt();
        this.voiceChatDistance = buf.readDouble();
        this.keepAlive = buf.readInt();
        this.groupsEnabled = buf.readBoolean();
        this.voiceHost = Utf8String.read(buf);
        this.allowRecording = buf.readBoolean();
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("secret", this.secret.toString());
        json.addProperty("serverPort", this.serverPort);
        json.addProperty("playerId", this.playerId.toString());
        json.addProperty("codec", this.codec.name());
        json.addProperty("mtuSize", this.mtuSize);
        json.addProperty("voiceChatDistance", this.voiceChatDistance);
        json.addProperty("keepAlive", this.keepAlive);
        json.addProperty("groupsEnabled", this.groupsEnabled);
        json.addProperty("voiceHost", this.voiceHost);
        json.addProperty("allowRecording", this.allowRecording);
    }

    @Override
    public void decode(JsonObject json) {
        this.secret = UUID.fromString(json.get("secret").getAsString());
        this.serverPort = json.get("serverPort").getAsInt();
        this.playerId = UUID.fromString(json.get("playerId").getAsString());
        this.codec = Codec.valueOf(json.get("codec").getAsString());
        this.mtuSize = json.get("mtuSize").getAsInt();
        this.voiceChatDistance = json.get("voiceChatDistance").getAsDouble();
        this.keepAlive = json.get("keepAlive").getAsInt();
        this.groupsEnabled = json.get("groupsEnabled").getAsBoolean();
        this.voiceHost = json.get("voiceHost").getAsString();
        this.allowRecording = json.get("allowRecording").getAsBoolean();
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleSecretPacket(player, this);
    }

    public UUID getSecret() {
        return this.secret;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public int getMtuSize() {
        return this.mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }

    public double getVoiceChatDistance() {
        return this.voiceChatDistance;
    }

    public void setVoiceChatDistance(double voiceChatDistance) {
        this.voiceChatDistance = voiceChatDistance;
    }

    public int getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isGroupsEnabled() {
        return this.groupsEnabled;
    }

    public void setGroupsEnabled(boolean groupsEnabled) {
        this.groupsEnabled = groupsEnabled;
    }

    public String getVoiceHost() {
        return this.voiceHost;
    }

    public void setVoiceHost(String voiceHost) {
        this.voiceHost = voiceHost;
    }

    public boolean isAllowRecording() {
        return this.allowRecording;
    }

    public void setAllowRecording(boolean allowRecording) {
        this.allowRecording = allowRecording;
    }

    @Override
    public String toString() {
        return "SecretSvcPacket{" +
                "secret=" + secret +
                ", serverPort=" + serverPort +
                ", playerId=" + playerId +
                ", codec=" + codec +
                ", mtuSize=" + mtuSize +
                ", voiceChatDistance=" + voiceChatDistance +
                ", keepAlive=" + keepAlive +
                ", groupsEnabled=" + groupsEnabled +
                ", voiceHost='" + voiceHost + '\'' +
                ", allowRecording=" + allowRecording +
                '}';
    }
}
