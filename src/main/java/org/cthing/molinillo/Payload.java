package org.cthing.molinillo;

import java.util.Objects;

import javax.annotation.Nullable;


/**
 * Represents the payload for a vertex of the dependency graph. This is a union type containing either a
 * possibility set or a specification.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class Payload<R, S> {

    @Nullable
    private final PossibilitySet<R, S> possibilitySet;

    @Nullable
    private final S specification;

    public Payload(final PossibilitySet<R, S> possibilitySet) {
        this.possibilitySet = possibilitySet;
        this.specification = null;
    }

    public Payload(final S specification) {
        this.possibilitySet = null;
        this.specification = specification;
    }

    /**
     * Obtains the possibility set held by the payload.
     *
     * @return Possibility set held by the payload
     * @throws IllegalStateException if the payload does not represent a possibility set
     */
    public PossibilitySet<R, S> getPossibilitySet() {
        assert this.possibilitySet != null;
        return this.possibilitySet;
    }

    /**
     * Obtains the specification held by the payload.
     *
     * @return Specification held by the payload
     * @throws IllegalStateException if the payload does not represent a specification
     */
    public S getSpecification() {
        assert this.specification != null;
        return this.specification;
    }

    /**
     * Indicates if the type of the payload is a PossibilitySet.
     *
     * @return {@code true} if the type of the payload is a PossibilitySet.
     */
    public boolean isPossibilitySet() {
        return this.possibilitySet != null;
    }

    /**
     * Indicates if the type of the payload is a specification.
     *
     * @return {@code true} if the type of the payload is a specification.
     */
    public boolean isSpecification() {
        return this.specification != null;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public String toString() {
        return (this.possibilitySet == null) ? this.specification.toString() : this.possibilitySet.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Payload<?, ?> payload = (Payload<?, ?>)obj;
        return Objects.equals(this.possibilitySet, payload.possibilitySet)
                && Objects.equals(this.specification, payload.specification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.possibilitySet, this.specification);
    }
}
