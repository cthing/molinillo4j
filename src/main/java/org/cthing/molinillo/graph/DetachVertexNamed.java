package org.cthing.molinillo.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.cthing.molinillo.DependencyGraph;
import org.jspecify.annotations.Nullable;


/**
 * Action to detach a vertex from the dependency graph. Detaching a vertex removes the vertex from the graph and
 * deletes all its incoming and outgoing edges. Any non-root orphaned vertices are also removed. Note that the
 * {@link #up(DependencyGraph)} method recursively invokes the {@link DependencyGraph#detachVertexNamed(String)}
 * method to remove orphaned vertices. This means that a single call to the {@link #up(DependencyGraph)} cannot
 * necessarily be fully undone with a single call to the {@link #down(DependencyGraph)} method. The action log
 * might need to be popped to full restore the state of the dependency graph.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class DetachVertexNamed<P, R> extends Action<P, R, List<Vertex<P, R>>> {

    private final String name;

    @Nullable
    private Vertex<P, R> vertex;

    /**
     * Constructs the action.
     *
     * @param name Name of the vertex to detach from the graph
     */
    public DetachVertexNamed(final String name) {
        this.name = name;
    }

    /**
     * Obtains the name of the vertex to detach from the graph.
     *
     * @return Name of the vertex to detach from the graph.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public List<Vertex<P, R>> up(final DependencyGraph<P, R> graph) {
        this.vertex = graph.getVertices().remove(this.name);
        if (this.vertex == null) {
            return List.of();
        }

        final List<Vertex<P, R>> removedVertices = new ArrayList<>();
        removedVertices.add(this.vertex);

        this.vertex.getOutgoingEdges().forEach(edge -> {
            final Vertex<P, R> v = edge.getDestination();
            v.getIncomingEdges().remove(edge);
            if (!v.isRoot() && v.getIncomingEdges().isEmpty()) {
                removedVertices.addAll(graph.detachVertexNamed(v.getName()));
            }
        });

        this.vertex.getIncomingEdges().forEach(edge -> {
            final Vertex<P, R> v = edge.getOrigin();
            v.getOutgoingEdges().remove(edge);
        });

        return removedVertices;
    }

    @Override
    public void down(final DependencyGraph<P, R> graph) {
        if (this.vertex == null) {
            return;
        }

        graph.getVertices().put(this.vertex.getName(), this.vertex);
        this.vertex.getOutgoingEdges().forEach(edge -> edge.getDestination().getIncomingEdges().add(edge));
        this.vertex.getIncomingEdges().forEach(edge -> edge.getOrigin().getOutgoingEdges().add(edge));
    }

    @Override
    public String toString() {
        return "DetachVertexNamed { name=" + this.name + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final DetachVertexNamed<?, ?> that = (DetachVertexNamed<?, ?>)obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
