package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@NullMarked
public class SvcPlayerCipherCodec extends SvcUdpPipelineNode<ByteBuf, ByteBuf> {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec key;

    private final Cipher encodeCipher;
    private final ByteBuf encodeIv;

    private final Cipher decodeCipher;
    private byte @Nullable [] lastDecodeIv;

    public SvcPlayerCipherCodec(SvcUdpMagicCodec svcCodec, UUID secret) {
        super(svcCodec);
        try {
            this.encodeIv = Unpooled.wrappedBuffer(generateIV());
            this.key = createKeySpec(secret);

            this.encodeCipher = Cipher.getInstance(CIPHER);
            this.encodeCipher.init(Cipher.ENCRYPT_MODE, this.key,
                    new IvParameterSpec(this.encodeIv.array()));
            this.decodeCipher = Cipher.getInstance(CIPHER);
        } catch (GeneralSecurityException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        RANDOM.nextBytes(iv);
        return iv;
    }

    private static SecretKeySpec createKeySpec(UUID secret) {
        byte[] key = ByteBuffer.wrap(new byte[16])
                .putLong(secret.getMostSignificantBits())
                .putLong(secret.getLeastSignificantBits())
                .array();
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, SvcUdpContext svcCtx) throws Exception {
        try {
            byte[] unciphered = new byte[msg.readableBytes()];
            msg.readBytes(unciphered);
            byte[] ciphered = this.encodeCipher.doFinal(unciphered);

            out.add(Unpooled.compositeBuffer(2)
                    .addComponent(true, this.encodeIv.retainedSlice())
                    .addComponent(true, Unpooled.wrappedBuffer(ciphered)));
        } finally {
            msg.release();
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, SvcUdpContext svcCtx) throws Exception {
        byte[] iv = new byte[16];
        msg.readBytes(iv);
        // if the IV hasn't changed, retain old decode cipher
        if (this.lastDecodeIv == null || !Arrays.equals(this.lastDecodeIv, iv)) {
            this.decodeCipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(iv));
            this.lastDecodeIv = iv;
        }

        try {
            byte[] ciphered = new byte[msg.readableBytes()];
            msg.readBytes(ciphered);
            byte[] unciphered = this.decodeCipher.doFinal(ciphered);
            out.add(Unpooled.wrappedBuffer(unciphered));
        } finally {
            msg.release();
        }
    }
}
