package dev.minceraft.sonus.plasmo.protocol.tcp.data.source;


import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.common.util.GameProfile;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.CodecInfo;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class DirectSourceInfo extends SourceInfo {

    private final GameProfile profile;
    private final Vec3d relativePosition;
    private final Vec3d lookAngle;
    private final boolean cameraRelative;

    public DirectSourceInfo(ByteBuf buf) {
        super(buf, SourceType.DIRECT);
        this.profile = DataTypeUtil.readIf(buf, b -> DataTypeUtil.INT.readGameProfile(b, Utf8String::readUnsignedShort));
        this.relativePosition = DataTypeUtil.readIf(buf, Vec3d::read);
        this.lookAngle = DataTypeUtil.readIf(buf, Vec3d::read);
        this.cameraRelative = buf.readBoolean();
    }

    public DirectSourceInfo(String addonId, UUID id, UUID voiceLineId, @Nullable String name,
                            byte state, @Nullable CodecInfo codecInfo, boolean stereo, boolean iconVisible, int angle,
                            GameProfile profile, Vec3d relativePosition, Vec3d lookAngle, boolean cameraRelative) {
        super(SourceType.DIRECT, addonId, id, voiceLineId, name, state, codecInfo, stereo, iconVisible, angle);
        this.profile = profile;
        this.relativePosition = relativePosition;
        this.lookAngle = lookAngle;
        this.cameraRelative = cameraRelative;
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        DataTypeUtil.writeNullable(buf, this.profile, (b, profile) ->
                DataTypeUtil.INT.writeGameProfile(b, profile, Utf8String::writeUnsignedShort));
        DataTypeUtil.writeNullable(buf, this.relativePosition, Vec3d::write);
        DataTypeUtil.writeNullable(buf, this.lookAngle, Vec3d::write);
        buf.writeBoolean(this.cameraRelative);
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public Vec3d getRelativePosition() {
        return this.relativePosition;
    }

    public Vec3d getLookAngle() {
        return this.lookAngle;
    }

    public boolean isCameraRelative() {
        return this.cameraRelative;
    }
}
