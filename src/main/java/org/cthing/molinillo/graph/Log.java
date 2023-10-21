package org.cthing.molinillo.graph;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.cthing.molinillo.DependencyGraph;


/**
 * A log of command that are executed on the dependency graph and can be undone.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class Log<P, R> implements Iterable<Action<P, R, ?>> {

    @Nullable
    private Action<P, R, ?> currentAction;

    @Nullable
    private Action<P, R, ?> firstAction;

    /**
     * Tags the current state of the dependency graph for possible undo.
     *
     * @param graph Dependency graph to be tagged
     * @param tagValue Opaque value to use as a tag
     */
    public void tag(final DependencyGraph<P, R> graph, final Object tagValue) {
        pushAction(graph, new Tag<>(tagValue));
    }

    /**
     * Undoes the log to the specified tag.
     *
     * @param graph Dependency graph on which to undo the actions.
     *
     * @param tagValue Value for the tag marking the point to which the graph should be undone.
     */
    public void rewindTo(final DependencyGraph<P, R> graph, final Object tagValue) {
        final Tag<P, R> tag = new Tag<>(tagValue);
        while (true) {
            final Action<P, R, ?> action = pop(graph);
            if (action == null) {
                throw new IllegalStateException("No tag " + tagValue + " found");
            }
            if ((action instanceof Tag) && action.equals(tag)) {
                break;
            }
        }
    }

    /**
     * Adds the specified vertex to the dependency graph for possible undo. If a vertex with the specified name
     * already exists in the graph, it is updated with the new payload and root flag.
     *
     * @param graph Dependency graph to which the vertex is to be added
     * @param name Name of the vertex
     * @param payload Payload for the vertex
     * @param root Indicates if the vertex is a root of the graph
     * @return The newly created or updated vertex
     */
    public Vertex<P, R> addVertex(final DependencyGraph<P, R> graph, final String name, final P payload,
                                  final boolean root) {
        return pushAction(graph, new AddVertex<>(name, payload, root));
    }

    /**
     * Removes the specified vertex from the dependency graph, all edges related to it and any orphaned non-root
     * vertices.
     *
     * @param graph Dependency graph from which the vertex is to be removed
     * @param name Name of the vertex to remove
     * @return All vertices removed (i.e. the specified one and any orphaned, non-root vertices).
     */
    public List<Vertex<P, R>> detachVertexNamed(final DependencyGraph<P, R> graph, final String name) {
        return pushAction(graph, new DetachVertexNamed<>(name));
    }

    /**
     * Adds an edge to the dependency graph. The edge is guaranteed not to create a cycle before this method is called.
     *
     * @param graph Dependency graph on which to create the edge
     * @param origin Name of the origin vertex
     * @param destination Name of the destination vertex
     * @param requirement Requirement to place on the edge
     * @return Newly added edge
     */
    public Edge<P, R> addEdgeNoCircular(final DependencyGraph<P, R> graph, final String origin,
                                        final String destination, final R requirement) {
        return pushAction(graph, new AddEdgeNoCircular<>(origin, destination, requirement));
    }

    /**
     * Sets the specified payload on the specified vertex.
     *
     * @param graph Dependency graph on which to perform the action
     * @param name Name of the vertex whose payload is to be set
     * @param payload Payload to set on the vertex
     */
    public void setPayload(final DependencyGraph<P, R> graph, final String name, final P payload) {
        pushAction(graph, new SetPayload<>(name, payload));
    }

    @Override
    public Iterator<Action<P, R, ?>> iterator() {
        return new Iterator<>() {
            @Nullable
            private Action<P, R, ?> action = Log.this.firstAction;

            @Override
            public boolean hasNext() {
                return this.action != null;
            }

            @Override
            public Action<P, R, ?> next() {
                final Action<P, R, ?> tmp = this.action;
                if (tmp == null) {
                    throw new NoSuchElementException();
                }
                this.action = this.action.getNext();
                return tmp;
            }
        };
    }

    /**
     * Removes the most recent action from the log and undoes its execution.
     *
     * @param graph Dependency graph on which to perform the undo action
     * @return The action that was removed from the log
     */
    @Nullable
    private Action<P, R, ?> pop(final DependencyGraph<P, R> graph) {
        if (this.currentAction == null) {
            return null;
        }

        final Action<P, R, ?> action = this.currentAction;
        this.currentAction = action.getPrevious();
        if (this.currentAction == null) {
            this.firstAction = null;
        }

        action.down(graph);
        return action;
    }

    /**
     * Adds the specified action to the log and executes it.
     *
     * @param graph Dependency graph on which to perform the action
     * @param action Action to be added to the log and executed
     */
    private <RETURN> RETURN pushAction(final DependencyGraph<P, R> graph, final Action<P, R, RETURN> action) {
        action.setPrevious(this.currentAction);

        final Action<P, R, ?> curAction = this.currentAction;
        if (curAction != null) {
            curAction.setNext(action);
        }
        this.currentAction = action;

        if (this.firstAction == null) {
            this.firstAction = action;
        }

        return action.up(graph);
    }
}
