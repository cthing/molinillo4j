package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * An error caused by searching for a dependency that is unknown (e.g. cannot be found in a repository or has no
 * published versions).
 */
public class NoSuchDependencyError extends ResolverError {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs the exception without specifying any dependents.
     *
     * @param dependency The dependency that could not be found
     */
    public NoSuchDependencyError(final Object dependency) {
        this(dependency, Set.of());
    }

    /**
     * Constructs the exception.
     *
     * @param dependency The dependency that could not be found
     * @param requiredBy The dependents
     */
    public NoSuchDependencyError(final Object dependency, final Set<Object> requiredBy) {
        super("Unable to find a specification for '"
                      + dependency
                      + (requiredBy.isEmpty()
                         ? "'"
                         : "' depended upon by "
                                 + requiredBy.stream().map(Object::toString).collect(Collectors.joining(" and "))));
    }
}
