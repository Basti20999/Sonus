package dev.minceraft.sonus.web.adapter.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class HttpErrorException extends RuntimeException {

    private final HttpResponseStatus status;

    public HttpErrorException(HttpResponseStatus status) {
        this.status = status;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // performance
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
