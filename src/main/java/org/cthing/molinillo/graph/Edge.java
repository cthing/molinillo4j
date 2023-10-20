package org.cthing.molinillo.graph;

/**
 * Represents a directed edge in the dependency graph. In addition to its origin and destination vertices, an
 * edge holds a requirement.
 *
 * @param <P> Payload type for the vertices at either end of the edge
 * @param <R> Requirement type
 * @param origin Starting point of the directed edge
 * @param destination End point of the directed edge
 * @param requirement Requirement placed on the destination by the origin (e.g. version constraint)
 */
public record Edge<P, R>(Vertex<P, R> origin, Vertex<P, R> destination, R requirement) {

    @Override
    public String toString() {
        return "Edge { " + this.origin + " -> " + this.destination + " (" + this.requirement + ") }";
    }
}
