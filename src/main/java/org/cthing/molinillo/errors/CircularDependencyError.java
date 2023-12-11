package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cthing.molinillo.graph.Vertex;


/**
 * An error caused by attempting to satisfy a circular dependency. Note that this exception is thrown if and only if
 * a vertex is added to a dependency graph that already has a path to an existing vertex.
 */
@SuppressWarnings("unchecked")
public class CircularDependencyError extends ResolverError {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Vertices comprising the circular path. */
    private final Collection<Vertex<?, ?>> vertices;

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

        this.vertices = new ArrayList<>(vertices);
    }

    /**
     * Obtains the non-null payloads in the vertices associated with the error.
     *
     * @param <P> Payload type
     * @return Non-null payloads
     */
    public <P> List<P> getPayloads() {
        return this.vertices.stream()
                            .filter(vertex -> vertex.getPayload().isPresent())
                            .map(vertex -> (P)(vertex.getPayload().get()))
                            .collect(Collectors.toList());
    }
}
