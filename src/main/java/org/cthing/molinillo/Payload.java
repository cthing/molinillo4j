package org.cthing.molinillo;

import java.util.Objects;

import javax.annotation.Nullable;


/**
 * Represents the payload for a vertex or edge of the dependency graph.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public final class Payload<R, S> {

    private enum UnionType {
        PS,
        SPEC
    }

    private final UnionType unionType;

    @Nullable
    private final PossibilitySet<R, S> possibilitySet;

    @Nullable
    private final S specification;

    private Payload(@Nullable final PossibilitySet<R, S> possibilitySet, @Nullable final S specification,
                    final UnionType unionType) {
        this.possibilitySet = possibilitySet;
        this.specification = specification;
        this.unionType = unionType;
    }

    /**
     * Creates a payload instance from the specified possibility set.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @param possSet Possibility set for the payload
     * @return Newly constructed payload holding the specified possibility set.
     */
    public static <R, S> Payload<R, S> fromPossibilitySet(@Nullable final PossibilitySet<R, S> possSet) {
        return new Payload<>(possSet, null, UnionType.PS);
    }

    /**
     * Creates a payload instance from the specified specification.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @param spec Specification for the payload
     * @return Newly constructed payload holding the specified specification.
     */
    public static <R, S> Payload<R, S> fromSpecification(@Nullable final S spec) {
        return new Payload<>(null, spec, UnionType.SPEC);
    }

    /**
     * Obtains the possibility set held by the payload.
     *
     * @return Possibility set held by the payload
     * @throws IllegalStateException if the payload does not represent a possibility set
     */
    @Nullable
    public PossibilitySet<R, S> getPossibilitySet() {
        if (this.unionType != UnionType.PS) {
            throw new IllegalStateException("BUG: Payload is a specification but expected a PossibilitySet");
        }

        return this.possibilitySet;
    }

    /**
     * Obtains the specification held by the payload.
     *
     * @return Specification held by the payload
     * @throws IllegalStateException if the payload does not represent a specification
     */
    @Nullable
    public S getSpecification() {
        if (this.unionType != UnionType.SPEC) {
            throw new IllegalStateException("BUG: Payload is a PossibilitySet but expected a specification");
        }

        return this.specification;
    }

    /**
     * Indicates whether the payload is {@code null}.
     *
     * @return {@code true} if the payload is {@code null}.
     */
    public boolean isEmpty() {
        return (this.unionType == UnionType.PS) ? (this.possibilitySet == null) : (this.specification == null);
    }

    /**
     * Indicates if the type of the payload is a PossibilitySet.
     *
     * @return {@code true} if the type of the payload is a PossibilitySet.
     */
    public boolean isPossibilitySet() {
        return this.unionType == UnionType.PS;
    }

    /**
     * Indicates if the type of the payload is a specification.
     *
     * @return {@code true} if the type of the payload is a specification.
     */
    public boolean isSpecification() {
        return this.unionType == UnionType.SPEC;
    }

    @Override
    public String toString() {
        if (this.unionType == UnionType.PS) {
            return (this.possibilitySet == null) ? "null" : this.possibilitySet.toString();
        } else {
            return (this.specification == null) ? "null" : this.specification.toString();
        }
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
        return this.unionType == payload.unionType
                && Objects.equals(this.possibilitySet, payload.possibilitySet)
                && Objects.equals(this.specification, payload.specification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.unionType, this.possibilitySet, this.specification);
    }
}
