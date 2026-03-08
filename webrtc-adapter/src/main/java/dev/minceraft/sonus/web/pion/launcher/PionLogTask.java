package dev.minceraft.sonus.web.pion.launcher;
// Created by booky10 in Sonus (00:39 08.03.2026)

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.BooleanSupplier;

@NullMarked
final class PionLogTask implements Runnable {

    private final BooleanSupplier running;
    private final Logger logger;
    private final BufferedReader reader;

    public PionLogTask(BooleanSupplier running, String loggerName, BufferedReader reader) {
        this.running = running;
        this.logger = LoggerFactory.getLogger(loggerName);
        this.reader = reader;
    }

    public static void launch(Process process, String loggerName) {
        Thread thread = new Thread(new PionLogTask(process::isAlive, loggerName, process.inputReader()));
        thread.setName("sonus-pion-logger");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try (BufferedReader reader = this.reader) {
            String line;
            while ((line = reader.readLine()) != null && this.running.getAsBoolean()) {
                this.logger.info(line);
            }
            this.logger.info("End of log has been reached");
        } catch (IOException exception) {
            if (this.running.getAsBoolean() && !"Stream closed".equals(exception.getMessage())) {
                this.logger.warn("Error while reading pion logs", exception);
            }
        }
    }
}
