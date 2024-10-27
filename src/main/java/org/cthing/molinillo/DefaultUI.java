package org.cthing.molinillo;


import org.jspecify.annotations.Nullable;


/**
 * Convenience base class for UI implementations. This default implementation is a no-op for all methods.
 */
public class DefaultUI implements UI {

    @Override
    public void indicateProgress() {
    }

    @Override
    public int getProgressRate() {
        return 0;
    }

    @Override
    public void beforeResolution() {
    }

    @Override
    public void afterResolution() {
    }

    @Override
    public void printf(final int depth, final String format, final @Nullable Object... args) {
    }

    @Override
    public boolean isDebugMode() {
        return false;
    }

    @Override
    public void setDebugMode(@Nullable final Boolean debugMode) {
    }
}
