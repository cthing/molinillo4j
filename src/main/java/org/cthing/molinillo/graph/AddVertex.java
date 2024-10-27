package org.cthing.molinillo.graph;

import java.util.Objects;
import java.util.Optional;

import org.cthing.molinillo.DependencyGraph;
import org.jspecify.annotations.Nullable;


/**
 * Action to add a vertex to the dependency graph and undo that action.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class AddVertex<P, R> extends Action<P, R, Vertex<P, R>> {

    @SuppressWarnings("InnerClassFieldHidesOuterClassField")
    private final class Existing {
        @Nullable
        final P payload;

        final boolean root;

        private Existing(@Nullable final P payload, final boolean root) {
            this.payload = payload;
            this.root = root;
        }
    }

    private final String name;

    @Nullable
    private final P payload;

    private final boolean root;

    @Nullable
    private Existing existing;

    /**
     * Constructs the action.
     *
     * @param name Name for the vertex
     * @param payload Payload for the vertex
     * @param root {@code true} if this is a root node of the graph
     */
    public AddVertex(final String name, @Nullable final P payload, final boolean root) {
        this.name = name;
        this.payload = payload;
        this.root = root;
    }

    /**
     * Obtains the name of the vertex to add.
     *
     * @return Vertex name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtains the payload to be set on the added vertex.
     *
     * @return Vertex payload
     */
    public Optional<P> getPayload() {
        return Optional.ofNullable(this.payload);
    }

    /**
     * Indicates whether the added vertex is a root of the dependency graph (i.e. will have no incoming edges).
     *
     * @return {@code true} if the added vertex is a root of the graph.
     */
    public boolean isRoot() {
        return this.root;
    }

    @Override
    public Vertex<P, R> up(final DependencyGraph<P, R> graph) {
        Vertex<P, R> vertex = graph.getVertices().get(this.name);
        if (vertex != null) {
            this.existing = new Existing(vertex.getPayload().orElse(null), vertex.isRoot());
            if (vertex.getPayload().isEmpty()) {
                vertex.setPayload(this.payload);
            }
            if (!vertex.isRoot()) {
                vertex.setRoot(this.root);
            }
        } else {
            vertex = new Vertex<>(this.name, this.payload);
            vertex.setRoot(this.root);
            graph.getVertices().put(this.name, vertex);
        }

        return vertex;
    }

    @Override
    public void down(final DependencyGraph<P, R> graph) {
        if (this.existing != null) {
            final Vertex<P, R> vertex = graph.getVertices().get(this.name);
            vertex.setPayload(this.existing.payload);
            vertex.setRoot(this.existing.root);
        } else {
            graph.getVertices().remove(this.name);
        }
    }

    @Override
    public String toString() {
        return "AddVertex { name=" + this.name + ", payload=" + this.payload + ", root=" + this.root + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final AddVertex<?, ?> addVertex = (AddVertex<?, ?>)obj;
        return this.root == addVertex.root
                && Objects.equals(this.name, addVertex.name)
                && Objects.equals(this.payload, addVertex.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.payload, this.root);
    }
}
