package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A collection of possibility states that share the same dependencies.
 *
 * @param <R> Type for a requirement
 * @param <S> Type for a specification
 */
public final class PossibilitySet<R, S> {

    private final Set<R> dependencies;
    private final List<S> possibilities;


    /**
     * Constructs the collection of possibility states.
     *
     * @param dependencies Dependencies for this set of possibilities
     * @param possibilities Possibility states for this set
     */
    public PossibilitySet(final Set<R> dependencies, final Collection<S> possibilities) {
        this.dependencies = new LinkedHashSet<>(dependencies);
        this.possibilities = new ArrayList<>(possibilities);
    }

    /**
     * Obtains the dependencies for this set of possibilities.
     *
     * @return Dependencies for the possibilities
     */
    public Set<R> getDependencies() {
        return this.dependencies;
    }

    /**
     * Obtains the possibilities comprising this set.
     *
     * @return Possibilities comprising this set
     */
    public List<S> getPossibilities() {
        return this.possibilities;
    }

    /**
     * Obtains the most recent possibility in the possibility set.
     *
     * @return Most recent possibility in the possibility set.
     */
    public Optional<S> getLatestVersion() {
        return this.possibilities.isEmpty()
               ? Optional.empty()
               : Optional.of(this.possibilities.get(this.possibilities.size() - 1));
    }

    @Override
    public String toString() {
        return "PossibilitySet { possibilities="
                + this.possibilities.stream().map(Object::toString).collect(Collectors.joining(", ")) + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final PossibilitySet<?, ?> that = (PossibilitySet<?, ?>)obj;
        return Objects.equals(this.dependencies, that.dependencies)
                && Objects.equals(this.possibilities, that.possibilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dependencies, this.possibilities);
    }
}
