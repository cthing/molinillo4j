package org.cthing.molinillo;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


/**
 * A state encapsulating a single possibility to fulfill the given requirement.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class PossibilityState<R, S> extends ResolutionState<R, S> {

    /**
     * Constructs a possibility state.
     *
     * @param name Name of the current requirement
     * @param requirements Currently unsatisfied requirements
     * @param activated Graph of activated dependencies
     * @param requirement Current requirement
     * @param possibility Possibility to satisfy the current requirement
     * @param depth Depth of the resolution
     * @param conflicts Unresolved conflicts, indexed by dependency name
     * @param unusedUnwindOptions Unwinds for previous conflicts that were not explored
     */
    public PossibilityState(final String name, final List<R> requirements,
                            final DependencyGraph<Payload<R, S>, R> activated, @Nullable final R requirement,
                            @Nullable final PossibilitySet<R, S> possibility, final int depth,
                            final Map<String, Conflict<R, S>> conflicts,
                            final List<UnwindDetails<R, S>> unusedUnwindOptions) {
        super(name, requirements, activated, requirement,
              (possibility == null) ? List.of() : List.of(possibility),
              depth, conflicts, unusedUnwindOptions);
    }
}
