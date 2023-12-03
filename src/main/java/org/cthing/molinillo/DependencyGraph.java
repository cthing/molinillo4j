package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.cthing.molinillo.annotations.VisibilityForTesting;
import org.cthing.molinillo.errors.CircularDependencyError;
import org.cthing.molinillo.graph.Edge;
import org.cthing.molinillo.graph.Log;
import org.cthing.molinillo.graph.Vertex;


/**
 * Represents a directed graph of specifications as vertices and edges as the requirements of one package on another.
 * An example of a specification is a software package and a requirement can be a version constraints.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class DependencyGraph<P, R> {

    private final Log<P, R> log;
    private final Map<String, Vertex<P, R>> vertices;

    /**
     * Constructs an empty dependency graph.
     */
    public DependencyGraph() {
        this(new Log<>());
    }

    /**
     * Constructs an empty dependency graph. This constructor is for testing purposes.
     *
     * @param log Action log to record changes to the graph
     */
    public DependencyGraph(final Log<P, R> log) {
        this.log = log;
        this.vertices = new HashMap<>();
    }

    /**
     * Obtains the vertices (i.e. dependencies) comprising the graph.
     *
     * @return A map of the name of a vertex to the vertex.
     */
    public Map<String, Vertex<P, R>> getVertices() {
        return this.vertices;
    }

    /**
     * Indicates if the dependency graph does not contain any vertices.
     *
     * @return {@code true} if the dependency graph does not contain any vertices.
     */
    public boolean isEmpty() {
        return this.vertices.isEmpty();
    }

    /**
     * Tags the current state of the dependency graph with the specified tag value.
     *
     * @param tagValue Opaque tag value for the current state of the graph
     */
    public void tag(final Object tagValue) {
        this.log.tag(this, tagValue);
    }

    /**
     * Undoes modifications of the dependency graph to the specified tag.
     *
     * @param tagValue Opaque tag value to rewind to
     * @throws IllegalStateException if the specified tag cannot be found in the log
     */
    public void rewindTo(final Object tagValue) {
        this.log.rewindTo(this, tagValue);
    }

    /**
     * Adds a vertex as a child of the specified parent (i.e. predecessor) vertices.
     *
     * @param name Name for the new vertex
     * @param payload Payload placed on the new vertex
     * @param parentNames The name of one or more parents for the new vertex.
     * @param requirement Requirement for the edges from the parent to the new vertex
     * @return Newly created vertex
     * @throws IllegalArgumentException if no parent vertices are specified
     */
    public Vertex<P, R> addChildVertex(final String name, @Nullable final P payload, final List<String> parentNames,
                                       final R requirement) {
        if (parentNames.isEmpty()) {
            throw new IllegalArgumentException("Parent vertices must be specified");
        }

        final Vertex<P, R> vertex = addVertex(name, payload, false);
        for (final String parentName : parentNames) {
            final Vertex<P, R> parentVertex = vertexNamed(parentName);
            assert parentVertex != null;
            addEdge(parentVertex, vertex, requirement);
        }
        return vertex;
    }

    /**
     * Adds a vertex to the dependency graph or updates and existing one.
     *
     * @param name Name of the vertex
     * @param payload Payload for the vertex
     * @param root Indicates whether the vertex is a root of the graph
     * @return The newly created or updated vertex
     */
    public Vertex<P, R> addVertex(final String name, @Nullable final P payload, final boolean root) {
        return this.log.addVertex(this, name, payload, root);
    }

    /**
     * Removes the specified vertex from the dependency graph, all edges related to it and any orphaned non-root
     * vertices.
     *
     * @param name Name of the vertex to remove
     * @return All vertices removed (i.e. the specified one and any orphaned, non-root vertices).
     */
    public List<Vertex<P, R>> detachVertexNamed(final String name) {
        return this.log.detachVertexNamed(this, name);
    }

    /**
     * Obtains the vertex with the specified name.
     *
     * @param name Name of the vertex to return
     * @return Vertex with the specified name or {@code null} if not found.
     */
    @Nullable
    public Vertex<P, R> vertexNamed(final String name) {
        return this.vertices.get(name);
    }

    /**
     * Adds a new edge to the dependency graph.
     *
     * @param origin Edge origin vertex
     * @param destination Edge destination vertex
     * @param requirement Requirement to place on the edge
     * @return Added edge
     */
    public Edge<P, R> addEdge(final Vertex<P, R> origin, final Vertex<P, R> destination, final R requirement) {
        if (destination.pathTo(origin)) {
            throw new CircularDependencyError(path(destination, origin));
        }
        return addEdgeNoCircular(origin, destination, requirement);
    }

    /**
     * Adds a new edge to the dependency graph without checking for a cycle.
     *
     * @param origin Edge origin vertex
     * @param destination Edge destination vertex
     * @param requirement Requirement to place on the edge
     * @return Added edge
     */
    private Edge<P, R> addEdgeNoCircular(final Vertex<P, R> origin, final Vertex<P, R> destination,
                                         final R requirement) {
        return this.log.addEdgeNoCircular(this, origin.getName(), destination.getName(), requirement);
    }

    /**
     * Sets the specified payload on the specified vertex.
     *
     * @param name Name of the vertex whose payload is to be set
     * @param payload Payload to set on the vertex
     */
    public void setPayload(final String name, final P payload) {
        this.log.setPayload(this, name, payload);
    }

    /**
     * Determines a path between the two specified vertices.
     *
     * @param from Starting vertex for the path
     * @param to Ending vertex for the path
     * @return Vertices comprising a path between the specified start and end vertices. The start and end vertices
     *      are included in the path
     * @throws IllegalArgumentException if no path exists between the specified vertices
     */
    @VisibilityForTesting
    List<Vertex<P, R>> path(final Vertex<P, R> from, final Vertex<P, R> to) {
        final Map<String, Integer> distances = new HashMap<>();
        distances.put(from.getName(), 0);

        final int defaultDistance = this.vertices.size() + 1;
        final Map<Vertex<P, R>, Vertex<P, R>> predecessors = new HashMap<>();
        this.vertices.values().forEach(vertex -> {
            final int vertexDistance = distances.getOrDefault(vertex.getName(), defaultDistance) + 1;
            vertex.successors().forEach(successor -> {
                if (distances.getOrDefault(successor.getName(), defaultDistance) > vertexDistance) {
                    distances.put(successor.getName(), vertexDistance);
                    predecessors.put(successor, vertex);
                }
            });
        });

        final List<Vertex<P, R>> path = new ArrayList<>();
        Vertex<P, R> destination = to;

        while (destination != null) {
            path.add(destination);

            if (destination.equals(from)) {
                break;
            }

            destination = predecessors.get(destination);
        }

        if (!path.get(path.size() - 1).equals(from)) {
            throw new IllegalArgumentException("There is no path from " + from.getName() + " to " + to.getName());
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Creates a string representation of the dependency graph in Graphviz DOT format.
     *
     * @return Graphviz DOT representation of the dependency graph.
     */
    public String toDot() {
        final Set<String> dotVertices = new TreeSet<>();
        final Set<String> dotEdges = new TreeSet<>();

        for (final Map.Entry<String, Vertex<P, R>> vertexEntry : this.vertices.entrySet()) {
            final String name = vertexEntry.getKey();
            final Vertex<P, R> vertex = vertexEntry.getValue();
            final Optional<P> payload = vertex.getPayload();
            if (payload.isPresent()) {
                dotVertices.add(String.format("%s [label=\"{%s|%s}\"]", name, name, payload.get()));
            } else {
                dotVertices.add(String.format("%s [label=\"{%s}\"]", name, name));
            }
            for (final Edge<P, R> edge : vertex.getOutgoingEdges()) {
                final String label = edge.getRequirement().toString();
                dotEdges.add(String.format("  %s -> %s [label=%s]", edge.getOrigin().getName(),
                                           edge.getDestination().getName(), label));
            }
        }

        return "digraph G {\n" + String.join("\n", dotVertices) + "\n" + String.join("\n", dotEdges) + "}\n";
    }

    @Override
    public String toString() {
        return "DependencyGraph { vertices=" + this.vertices.size() + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final DependencyGraph<?, ?> that = (DependencyGraph<?, ?>)obj;
        return Objects.equals(this.vertices, that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vertices);
    }
}
