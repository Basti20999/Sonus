package dev.minceraft.sonus.web.adapter.util;
// Created by booky10 in Sonus (9:38 PM 02.03.2026)

import dev.minceraft.sonus.common.data.RotatedVec3d;
import dev.minceraft.sonus.common.data.Vec3d;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AudioSpatialToStereoUtil {

    private static final double DEG_TO_RAD = Math.PI / 180d;

    private AudioSpatialToStereoUtil() {
    }

    // no proper ITD or ILD calculations, but good enough
    public static void process(
            short[] audio, Vec3d srcPos, RotatedVec3d listenerPos,
            short[] dstLeftAudio, short[] dstRightAudio
    ) {
        float yaw = listenerPos.getYaw();
        float pitch = listenerPos.getPitch();

        double diffX = srcPos.getX() - listenerPos.getX();
        double diffY = srcPos.getY() - listenerPos.getY();
        double diffZ = srcPos.getZ() - listenerPos.getZ();

        // rotate around y
        double cosYaw = Math.cos(yaw * -DEG_TO_RAD);
        double sinYaw = Math.sin(pitch * -DEG_TO_RAD);
        double lx = cosYaw * diffX - sinYaw * diffZ;
        double zz = sinYaw * diffX + cosYaw * diffZ;

        // rotate around x
        double cosPitch = Math.cos(pitch * -DEG_TO_RAD);
        double sinPitch = Math.sin(pitch * -DEG_TO_RAD);
        double ly = cosPitch * diffY - sinPitch * zz;
        double lz = sinPitch * diffY + cosPitch * zz;

        // distance attenuation
        double distance = Math.sqrt(lx * lx + ly * ly + lz * lz);
        double refDist = 1;
        double linearRolloff = 1;
        double distanceGain = refDist / (refDist + linearRolloff * Math.max(0, distance - refDist));

        // panning
        double pan = Math.atan2(lx, lz) / (Math.PI / 2);
        double angle = (pan + 1) * (Math.PI / 4);
        double leftGain = Math.abs(Math.cos(angle));
        double rightGain = Math.abs(Math.sin(angle));

        // calculate final gain
        leftGain *= distanceGain;
        rightGain *= distanceGain;

        // apply left/right gain
        for (int i = 0, len = audio.length; i < len; ++i) {
            short s = audio[i];
            dstLeftAudio[i] = (short) (s * leftGain);
            dstRightAudio[i] = (short) (s * rightGain);
        }
    }
}
