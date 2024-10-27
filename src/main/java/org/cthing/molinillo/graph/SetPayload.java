package org.cthing.molinillo.graph;

import java.util.Objects;
import java.util.Optional;

import org.cthing.molinillo.DependencyGraph;
import org.jspecify.annotations.Nullable;


/**
 * Action that sets a payload on a dependency graph vertex.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class SetPayload<P, R> extends Action<P, R, Void> {

    private final String name;

    @Nullable
    private final P payload;

    @Nullable
    private P oldPayload;

    /**
     * Constructs the action to set the specified payload on the specified vertex.
     *
     * @param name Name of the vertex whose payload is to be set
     * @param payload Payload to set on the vertex
     */
    public SetPayload(final String name, @Nullable final P payload) {
        this.name = name;
        this.payload = payload;
    }

    /**
     * Obtains the name of the vertex on which the payload is set.
     *
     * @return Name of the vertex on which the payload is set.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtains the payload that was set on the vertex.
     *
     * @return Payload set on the vertex.
     */
    public Optional<P> getPayload() {
        return Optional.ofNullable(this.payload);
    }

    @Override
    public Void up(final DependencyGraph<P, R> graph) {
        final Vertex<P, R> vertex = graph.vertexNamed(this.name).orElseThrow();
        this.oldPayload = vertex.getPayload().orElse(null);
        vertex.setPayload(this.payload);
        return null;
    }

    @Override
    public void down(final DependencyGraph<P, R> graph) {
        final Vertex<P, R> vertex = graph.vertexNamed(this.name).orElseThrow();
        vertex.setPayload(this.oldPayload);
    }

    @Override
    public String toString() {
        return "SetPayload { name=" + this.name + ", payload=" + this.payload + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SetPayload<?, ?> that = (SetPayload<?, ?>)obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.payload);
    }
}
