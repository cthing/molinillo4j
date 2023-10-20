package org.cthing.molinillo.errors;

import java.io.Serial;


/**
 * Base class for all resolution exceptions.
 */
public class ResolverError extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ResolverError(final String message) {
        super(message);
    }
}
