package org.cthing.molinillo;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;


/**
 * Provides output from the resolution process to the user using the console.
 */
public class ConsoleUI implements UI {

    private static final PrintWriter WRITER = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
    private static final int PROGRESS_RATE = 333;   // Progress output frequency in milliseconds

    @Nullable
    private Boolean debugMode;

    @Override
    public PrintWriter getOutput() {
        return WRITER;
    }

    @Override
    public void indicateProgress() {
        if (!isDebugMode()) {
            WRITER.print('.');
        }
    }

    @Override
    public int getProgressRate() {
        return PROGRESS_RATE;
    }

    @Override
    public void beforeResolution() {
        WRITER.println("Resolving dependencies...");
    }

    @Override
    public void afterResolution() {
        WRITER.println();
    }

    @Override
    public void debug(final int depth, final String format, final Object... args) {
        if (isDebugMode()) {
            WRITER.println(":" + depth + ":" + String.format(format, args));
        }
    }

    @Override
    public boolean isDebugMode() {
        if (this.debugMode == null) {
            this.debugMode = (System.getenv("MOLINILLO_DEBUG") != null);
        }
        return this.debugMode;
    }

    @Override
    public void setDebugMode(@Nullable final Boolean debugMode) {
        this.debugMode = debugMode;
    }
}
