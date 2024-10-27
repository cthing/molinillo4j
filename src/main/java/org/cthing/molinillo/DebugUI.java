package org.cthing.molinillo;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;


/**
 * Provides debugging output to the console during the dependency resolution process. Debug output can be enabled
 * either by calling {@code setDebugMode(true)} or setting the {@code MOLINILLO_DEBUG} environment variable to any
 * value.
 */
public class DebugUI implements UI {

    private static final PrintWriter WRITER = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
    private static final int PROGRESS_RATE = 333;   // Progress output frequency in milliseconds

    @Nullable
    private Boolean debugMode;

    @Override
    public void indicateProgress() {
        if (isDebugMode()) {
            WRITER.print('.');
        }
    }

    @Override
    public int getProgressRate() {
        return PROGRESS_RATE;
    }

    @Override
    public void beforeResolution() {
        if (isDebugMode()) {
            WRITER.println("Resolving dependencies...");
        }
    }

    @Override
    public void afterResolution() {
        if (isDebugMode()) {
            WRITER.println();
        }
    }

    @Override
    public void printf(final int depth, final String format, final @Nullable Object... args) {
        if (isDebugMode()) {
            WRITER.println(String.format(":%4d: ", depth) + String.format(format, args));
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
