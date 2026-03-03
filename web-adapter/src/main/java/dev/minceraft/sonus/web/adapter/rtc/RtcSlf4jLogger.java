package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (7:08 PM 02.03.2026)

import dev.onvoid.webrtc.logging.LogSink;
import dev.onvoid.webrtc.logging.Logging;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class RtcSlf4jLogger implements LogSink {

    private final Logger logger;

    public RtcSlf4jLogger(Logger logger) {
        this.logger = logger;
    }

    public static void register(Logging.Severity severity, String loggerName) {
        Logger logger = LoggerFactory.getLogger(loggerName);
        Logging.addLogSink(severity, new RtcSlf4jLogger(logger));
    }

    @Override
    public void onLogMessage(Logging.Severity severity, String message) {
        switch (severity) {
            case NONE -> this.logger.trace(message.trim());
            case VERBOSE -> this.logger.debug(message.trim());
            case INFO -> this.logger.info(message.trim());
            case WARNING -> this.logger.warn(message.trim());
            case ERROR -> this.logger.error(message.trim());
        }
    }
}
