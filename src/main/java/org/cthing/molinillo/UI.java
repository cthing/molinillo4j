package org.cthing.molinillo;

import java.io.PrintWriter;

import javax.annotation.Nullable;


/**
 * Classes which provides output from the resolution process to the user must implement this interface.
 */
public interface UI {
    /**
     * Obtains the writer used to print output.
     *
     * @return Object to write output. Output is written to the standard output by default.
     */
    PrintWriter getOutput();

    /**
     * Called periodically according to {@link #getProgressRate()} to convey progress to the user. Does
     * not output anything if debug mode is enabled.
     */
    void indicateProgress();

    /**
     * Indicates how often progress should be conveyed to the user by the {@link #indicateProgress()} method.
     *
     * @return Progress rate in milliseconds. The default is 333 milliseconds (i.e. 1/3 of a second).
     */
    int getProgressRate();

    /**
     * Called before resolution begins.
     */
    void beforeResolution();

    /**
     * Called after resolution ends (either successfully or with an error). Prints a newline by default.
     */
    void afterResolution();

    /**
     * Writes debugging output.
     *
     * @param depth Current depth of the resolution process
     * @param format Output string passed to {@link String#format(String, Object...)}
     * @param args Arguments for the output string passed to {@link String#format(String, Object...)}
     */
    void debug(int depth, String format, Object... args);

    /**
     * Indicates whether debug messages should be printed. If the {@code MOLINILLO_DEBUG} environment variable
     * is set and a debug mode has not been set programmatically, debug mode will be enabled.
     *
     * @return {@code true} if debug mode is enabled.
     */
    boolean isDebugMode();

    /**
     * Sets whether debug mode is enabled. If {@code NULL} is specified, debug mode will be enabled if the
     * {@code MOLINILLO_DEBUG} environment variable is set.
     *
     * @param debugMode Whether debug mode should be enabled.
     */
    void setDebugMode(@Nullable Boolean debugMode);
}
