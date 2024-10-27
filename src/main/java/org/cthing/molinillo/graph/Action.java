package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.jspecify.annotations.Nullable;


/**
 * Base class for a reversible action that modifies the dependency graph.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 * @param <RETURN> Return type of the up action
 */
public abstract class Action<P, R, RETURN> {

    @Nullable
    private Action<P, R, ?> previous;

    @Nullable
    private Action<P, R, ?> next;

    /**
     * Obtains the previous action.
     *
     * @return Previous action or {@code null} if there is no previous action.
     */
    @Nullable
    public Action<P, R, ?> getPrevious() {
        return this.previous;
    }

    /**
     * Sets the previous action.
     *
     * @param previous Previous action or {@code null} to indicate there is no previous action
     */
    public void setPrevious(@Nullable final Action<P, R, ?> previous) {
        this.previous = previous;
    }

    /**
     * Obtains the next action.
     *
     * @return Next action or {@code null} if there is no next action.
     */
    @Nullable
    public Action<P, R, ?> getNext() {
        return this.next;
    }

    /**
     * Sets the next action.
     *
     * @param next Next action or {@code null} to indicate there is no next action
     */
    public void setNext(@Nullable final Action<P, R, ?> next) {
        this.next = next;
    }

    /**
     * Performs an action on the specified graph.
     *
     * @param graph Dependency graph on which to perform the action
     * @return Return type specified when action created
     */
    public abstract RETURN up(DependencyGraph<P, R> graph);

    /**
     * Reverses an action previously performed on the specified graph.
     *
     * @param graph Dependency graph on which to reverse the previous action
     */
    public abstract void down(DependencyGraph<P, R> graph);
}
