package dev.minceraft.sonus.common.natives;
// Created by booky10 in Sonus (20:53 20.12.2025)

import dev.minceraft.sonus.common.audio.AudioProcessor;
import org.jspecify.annotations.NullMarked;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

@NullMarked
public class OpusNativesLoader extends NativesLoader {

    private final Class<?> applicationClass;

    private final MethodHandle encoderCtor;
    private final MethodHandle encoderResetState;
    private final MethodHandle encoderSetMaxPayloadSize;
    private final MethodHandle encoderSetMaxPacketLossPercentage;
    private final MethodHandle encoderEncode;
    private final MethodHandle encoderClose;

    private final MethodHandle decoderCtor;
    private final MethodHandle decoderResetState;
    private final MethodHandle decoderSetFrameSize;
    private final MethodHandle decoderDecode;
    private final MethodHandle decoderClose;

    public OpusNativesLoader() {
        super("opus4j.jar");
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            this.applicationClass = this.loadClass("de.maxhenkel.opus4j.OpusEncoder$Application");

            Class<?> encoderClass = this.loadClass("de.maxhenkel.opus4j.OpusEncoder");
            this.encoderCtor = lookup.findConstructor(encoderClass,
                    methodType(void.class, int.class, int.class, this.applicationClass));
            this.encoderResetState = lookup.findVirtual(encoderClass, "resetState",
                    methodType(void.class));
            this.encoderSetMaxPayloadSize = lookup.findVirtual(encoderClass, "setMaxPayloadSize",
                    methodType(void.class, int.class));
            this.encoderSetMaxPacketLossPercentage = lookup.findVirtual(encoderClass, "setMaxPacketLossPercentage",
                    methodType(void.class, float.class));
            this.encoderEncode = lookup.findVirtual(encoderClass, "encode",
                    methodType(byte[].class, short[].class));
            this.encoderClose = lookup.findVirtual(encoderClass, "close",
                    methodType(void.class));

            Class<?> decoderClass = this.loadClass("de.maxhenkel.opus4j.OpusDecoder");
            this.decoderCtor = lookup.findConstructor(decoderClass,
                    methodType(void.class, int.class, int.class));
            this.decoderResetState = lookup.findVirtual(decoderClass, "resetState",
                    methodType(void.class));
            this.decoderSetFrameSize = lookup.findVirtual(decoderClass, "setFrameSize",
                    methodType(void.class, int.class));
            this.decoderDecode = lookup.findVirtual(decoderClass, "decode",
                    methodType(short[].class, byte[].class));
            this.decoderClose = lookup.findVirtual(decoderClass, "close",
                    methodType(void.class));
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Object convertMode(AudioProcessor.Mode mode) {
        return this.applicationClass.getEnumConstants()[mode.ordinal()];
    }

    public final class Encoder implements AutoCloseable {

        private final MethodHandle resetState;
        private final MethodHandle setMaxPayloadSize;
        private final MethodHandle setMaxPacketLossPercentage;
        private final MethodHandle encode;
        private final MethodHandle close;


        public Encoder(int sampleRate, int channels, AudioProcessor.Mode mode) {
            Object encoder = unchecked(() -> OpusNativesLoader.this.encoderCtor.invoke(
                    sampleRate, channels, OpusNativesLoader.this.convertMode(mode)));
            this.resetState = OpusNativesLoader.this.encoderResetState.bindTo(encoder);
            this.setMaxPayloadSize = OpusNativesLoader.this.encoderSetMaxPayloadSize.bindTo(encoder);
            this.setMaxPacketLossPercentage = OpusNativesLoader.this.encoderSetMaxPacketLossPercentage.bindTo(encoder);
            this.encode = OpusNativesLoader.this.encoderEncode.bindTo(encoder);
            this.close = OpusNativesLoader.this.encoderClose.bindTo(encoder);
        }

        public void resetState() {
            unchecked(() -> this.resetState.invoke());
        }

        public void setMaxPayloadSize(int mtu) {
            unchecked(() -> this.setMaxPayloadSize.invoke(mtu));
        }

        public void setMaxPacketLossPercentage(float percentage) {
            unchecked(() -> this.setMaxPacketLossPercentage.invoke(percentage));
        }

        public byte[] encode(short[] pcm) {
            return unchecked(() -> (byte[]) this.encode.invoke(pcm));
        }

        @Override
        public void close() {
            unchecked(() -> this.close.invoke());
        }
    }

    public final class Decoder implements AutoCloseable {

        private final MethodHandle resetState;
        private final MethodHandle setFrameSize;
        private final MethodHandle decode;
        private final MethodHandle close;

        public Decoder(int sampleRate, int channels) {
            Object decoder = unchecked(() -> OpusNativesLoader.this.decoderCtor.invoke(sampleRate, channels));
            this.resetState = OpusNativesLoader.this.decoderResetState.bindTo(decoder);
            this.setFrameSize = OpusNativesLoader.this.decoderSetFrameSize.bindTo(decoder);
            this.decode = OpusNativesLoader.this.decoderDecode.bindTo(decoder);
            this.close = OpusNativesLoader.this.decoderClose.bindTo(decoder);
        }

        public void resetState() {
            unchecked(() -> this.resetState.invoke());
        }

        public void setFrameSize(int frameSize) {
            unchecked(() -> this.setFrameSize.invoke(frameSize));
        }

        public short[] decode(byte[] opus) {
            return unchecked(() -> (short[]) this.decode.invoke(opus));
        }

        @Override
        public void close() {
            unchecked(() -> this.close.invoke());
        }
    }
}
