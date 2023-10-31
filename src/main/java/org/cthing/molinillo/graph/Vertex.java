package org.cthing.molinillo.graph;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Represents a vertex in the dependency graph.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class Vertex<P, R> {

    private String name;
    private P payload;
    private boolean root;
    private final Set<R> explicitRequirements;
    private final Set<Edge<P, R>> outgoingEdges;
    private final Set<Edge<P, R>> incomingEdges;

    /**
     * Constructs a vertex with the specified name and payload.
     *
     * @param name Identifier for the vertex
     * @param payload User defined data held by the vertex
     */
    public Vertex(final String name, final P payload) {
        this.name = name;
        this.payload = payload;
        this.root = false;
        this.explicitRequirements = new HashSet<>();
        this.outgoingEdges = new HashSet<>();
        this.incomingEdges = new HashSet<>();
    }

    /**
     * Obtains the identifier for the vertex.
     *
     * @return Identifier for the vertex.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the identifier for the vertex.
     *
     * @param name Identifier for the vertex
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Obtains the user defined data held by the vertex.
     *
     * @return User defined data held by the vertex.
     */
    public P getPayload() {
        return this.payload;
    }

    /**
     * Sets the user defined data to be held by the vertex.
     *
     * @param payload User defined data to be held by the vertex
     */
    public void setPayload(final P payload) {
        this.payload = payload;
    }

    /**
     * Indicates whether this is the root vertex of the dependency graph.
     *
     * @return {@code true} if the vertex is the root of the dependency graph.
     */
    public boolean isRoot() {
        return this.root;
    }

    /**
     * Sets whether this is the root vertex of the dependency graph.
     *
     * @param root {@code true} to mark this vertex as the root of the dependency graph
     */
    public void setRoot(final boolean root) {
        this.root = root;
    }

    /**
     * Obtains the requirements placed directly on this vertex.
     *
     * @return Requirements placed directly on this vertex.
     */
    public Set<R> getExplicitRequirements() {
        return this.explicitRequirements;
    }

    /**
     * Obtains the edges pointing outward from this vertex.
     *
     * @return Edges pointing outward from this vertex.
     */
    public Set<Edge<P, R>> getOutgoingEdges() {
        return this.outgoingEdges;
    }

    /**
     * Obtains the edges pointing at this vertex.
     *
     * @return Edges pointing at this vertex.
     */
    public Set<Edge<P, R>> getIncomingEdges() {
        return this.incomingEdges;
    }

    /**
     * Obtains the combined requirements of this vertex and all incoming edges.
     *
     * @return Requirements of this vertex and all incoming edges.
     */
    public Set<R> requirements() {
        final Stream<R> requirements1 = this.incomingEdges.stream().map(Edge::getRequirement);
        final Stream<R> requitements2 = this.explicitRequirements.stream();
        return Stream.concat(requirements1, requitements2).collect(Collectors.toSet());
    }

    /**
     * Obtains all vertices pointing to this vertex.
     *
     * @return All vertices pointing to this vertex.
     */
    public Set<Vertex<P, R>> predecessors() {
        return this.incomingEdges.stream().map(Edge::getOrigin).collect(Collectors.toSet());
    }

    /**
     * Obtains all the vertices pointed to by this vertex.
     *
     * @return All vertices pointed to by this vertex.
     */
    public Set<Vertex<P, R>> successors() {
        return this.outgoingEdges.stream().map(Edge::getDestination).collect(Collectors.toSet());
    }

    /**
     * Determines if there is a path from this vertex to the specified vertex. This method handles
     * the case where there is a cycle in the graph.
     *
     * @param other The vertex to which a path is to be found
     * @return {@code true} if a path exists between this vertex and the specified vertex.
     */
    public boolean pathTo(final Vertex<P, R> other) {
        return pathTo(other, new HashSet<>());
    }

    /**
     * Determines if there is a path from this vertex to the specified vertex. This method handles
     * the case where there is a cycle in the graph.
     *
     * @param other The vertex to which a path is to be found
     * @param visited Set of vertices that have been visited. This is used to handle cycles in the graph.
     * @return {@code true} if a path exists between this vertex and the specified vertex.
     */
    private boolean pathTo(final Vertex<P, R> other, final Set<Vertex<P, R>> visited) {
        final boolean added = visited.add(this);

        // Handle a cycle in the graph. If the other vertex has not been found, stop searching when
        // visiting an already visited vertex.
        if (!added) {
            return false;
        }

        // A path to the other vertex has been found.
        if (equals(other)) {
            return true;
        }

        // Recursively search the graph.
        return successors().stream().anyMatch(vertex -> vertex.pathTo(other, visited));
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Vertex<?, ?> other = (Vertex<?, ?>)obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.payload, other.payload)
                && Objects.equals(successors(), other.successors());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
