package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.Collection;
import java.util.stream.Collectors;

import org.cthing.molinillo.graph.Vertex;


/**
 * An error caused by attempting to satisfy a circular dependency. Note that this exception is thrown if and only if
 * a vertex is added to a dependency graph that already has a path to an existing vertex.
 */
public class CircularDependencyError extends ResolverError {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a circular dependency exception.
     *
     * @param vertices Vertices comprising the circular path
     * @param <P> Payload type
     * @param <R> Requirement type
     */
    public <P, R> CircularDependencyError(final Collection<Vertex<P, R>> vertices) {
        super("There is a circular dependency between "
                      + vertices.stream().map(Vertex::toString).collect(Collectors.joining(" and ")));
    }
}
