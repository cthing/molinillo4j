package org.cthing.molinillo.graph;

import java.util.Objects;

import org.cthing.molinillo.DependencyGraph;


/**
 * An action for marking a position in the action log. This allows the action log to be undone to a specified tag.
 *
 * @param <P> Payload type
 * @param <R> Requirement type
 */
public class Tag<P, R> extends Action<P, R, Void> {

    private final int value;

    /**
     * Creates a tag for the specified object. If the object is a {@link String}, it is treated as a symbolic
     * constant. This means that two tags containing different string instances but with the same content, will be
     * considered the same tag. Objects that are not strings are considered equal by identity. That is, two separate
     * instances of the same class are not equal. In other words, objects are treated the same as the default
     * {@link Object#equals(Object)} method implementation.
     *
     * @param value Object representing the tag.
     */
    public Tag(final Object value) {
        this.value = (value instanceof String) ? value.hashCode() : System.identityHashCode(value);
    }

    /**
     * Obtains a hash representing this tag's value.
     *
     * @return Hash representing the tag's value. For strings, this is the result of calling {@link String#hashCode()}
     *      on the string. For other objects, this is the result of calling {@link System#identityHashCode(Object)} on
     *      that object.
     */
    public int getValue() {
        return this.value;
    }

    @Override
    public Void up(final DependencyGraph<P, R> graph) {
        return null;
    }

    @Override
    public void down(final DependencyGraph<P, R> graph) {
    }

    @Override
    public String toString() {
        return "Tag { value=" + this.value + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return this.value == ((Tag<?, ?>)obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
