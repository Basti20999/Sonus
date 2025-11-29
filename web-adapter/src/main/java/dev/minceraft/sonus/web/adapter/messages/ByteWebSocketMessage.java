
package dev.minceraft.sonus.web.adapter.messages;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public class ByteWebSocketMessage extends AbstractWebSocketMessage implements ReferenceCounted {

    private final ByteBuf buf;

    public ByteWebSocketMessage(ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf getBuf() {
        return this.buf.slice();
    }

    public ByteBuf getDirectBuf() {
        return this.buf;
    }

    @Override
    public int refCnt() {
        return this.buf.refCnt();
    }

    @Override
    public boolean release() {
        return this.buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.buf.release(decrement);
    }

    @Override
    public ByteWebSocketMessage touch() {
         this.buf.touch();
        return this;
    }

    @Override
    public ByteWebSocketMessage touch(Object hint) {
         this.buf.touch(hint);
        return this;
    }

    @Override
    public ByteWebSocketMessage retain() {
        this.buf.retain();
        return this;
    }

    @Override
    public ByteWebSocketMessage retain(int increment) {
        this.buf.retain(increment);
        return this;
    }
}
