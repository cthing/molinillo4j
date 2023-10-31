package org.cthing.molinillo.graph;

import java.util.Objects;

/**
 * Represents a directed edge in the dependency graph. In addition to its origin and destination vertices, an
 * edge holds a requirement.
 *
 * @param <P> Payload type for the vertices at either end of the edge
 * @param <R> Requirement type
 */
public class Edge<P, R> {

    private final Vertex<P, R> origin;
    private final Vertex<P, R> destination;
    private final R requirement;

    /**
     * Constructs and edge between the specified origin and destination vertices.
     *
     * @param origin Starting point of the directed edge
     * @param destination End point of the directed edge
     * @param requirement Requirement placed on the destination by the origin (e.g. version constraint)
     */
    public Edge(final Vertex<P, R> origin, final Vertex<P, R> destination, final R requirement) {
        this.origin = origin;
        this.destination = destination;
        this.requirement = requirement;
    }

    /**
     * Obtains the origin vertex of the edge.
     *
     * @return Origin vertex
     */
    public Vertex<P, R> getOrigin() {
        return this.origin;
    }

    /**
     * Obtains the destination vertex of the edge.
     *
     * @return Destination vertex
     */
    public Vertex<P, R> getDestination() {
        return this.destination;
    }

    /**
     * Obtains the requirement assigned to the edge.
     *
     * @return Requirement assigned to the edge.
     */
    public R getRequirement() {
        return this.requirement;
    }

    @Override
    public String toString() {
        return "Edge { " + this.origin + " -> " + this.destination + " (" + this.requirement + ") }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final Edge<?, ?> other = (Edge<?, ?>)obj;
        return Objects.equals(this.origin, other.origin)
                && Objects.equals(this.destination, other.destination)
                && Objects.equals(this.requirement, other.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.origin, this.destination, this.requirement);
    }
}
