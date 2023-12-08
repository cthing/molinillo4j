package org.cthing.molinillo.graph;

import java.util.Objects;

import org.cthing.molinillo.DependencyGraph;


/**
 * Action to add an edge to the dependency graph. The edge is guaranteed not to create a cycle before it is called.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class AddEdgeNoCircular<P, R> extends Action<P, R, Edge<P, R>> {

    private final String origin;
    private final String destination;
    private final R requirement;

    /**
     * Creates the action.
     *
     * @param origin Name of the origin vertex
     * @param destination Name of the destination vertex
     * @param requirement Requirement to place on the edge.
     */
    public AddEdgeNoCircular(final String origin, final String destination, final R requirement) {
        this.origin = origin;
        this.destination = destination;
        this.requirement = requirement;
    }

    /**
     * Obtains the name of the edge's origin vertex.
     *
     * @return Name of the origin vertex.
     */
    public String getOrigin() {
        return this.origin;
    }

    /**
     * Obtains the name of the edge's destination vertex.
     *
     * @return Name of the destination vertex.
     */
    public String getDestination() {
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
    public Edge<P, R> up(final DependencyGraph<P, R> graph) {
        final Edge<P, R> edge = makeEdge(graph);
        edge.getOrigin().getOutgoingEdges().add(edge);
        edge.getDestination().getIncomingEdges().add(edge);
        return edge;
    }

    @Override
    public void down(final DependencyGraph<P, R> graph) {
        final Edge<P, R> edge = makeEdge(graph);
        edge.getOrigin().getOutgoingEdges().remove(edge);
        edge.getDestination().getIncomingEdges().remove(edge);
    }

    /**
     * Creates an edge with the specified origin and destination vertices.
     *
     * @param graph Dependency graph on which to create the edge
     * @return Newly created edge
     */
    private Edge<P, R> makeEdge(final DependencyGraph<P, R> graph) {
        final Vertex<P, R> originVertex = graph.vertexNamed(this.origin).orElseThrow();
        final Vertex<P, R> destinationVertex = graph.vertexNamed(this.destination).orElseThrow();
        return new Edge<>(originVertex, destinationVertex, this.requirement);
    }

    @Override
    public String toString() {
        return "AddEdgeNoCircular { origin=" + this.origin + ", destination=" + this.destination
                + ", requirement=" + this.requirement + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AddEdgeNoCircular<?, ?> that = (AddEdgeNoCircular<?, ?>)obj;
        return Objects.equals(this.origin, that.origin) && Objects.equals(this.destination, that.destination) && Objects.equals(
                this.requirement, that.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.origin, this.destination, this.requirement);
    }
}
