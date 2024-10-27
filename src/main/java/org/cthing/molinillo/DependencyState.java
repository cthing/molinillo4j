package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;


/**
 * A state that encapsulates a set of requirements with a list of possibilities.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class DependencyState<R, S> extends ResolutionState<R, S> {

    /**
     * Constructs a dependency state.
     *
     * @param name Name of the current requirement
     * @param requirements Currently unsatisfied requirements
     * @param activated Graph of activated dependencies
     * @param requirement Current requirement
     * @param possibilities Possibilities to satisfy the current requirement
     * @param depth Depth of the resolution
     * @param conflicts Unresolved conflicts, indexed by dependency name
     * @param unusedUnwindOptions Unwinds for previous conflicts that were not explored
     */
    public DependencyState(final String name, final List<R> requirements,
                           final DependencyGraph<Payload<R, S>, R> activated, @Nullable final R requirement,
                           final List<PossibilitySet<R, S>> possibilities, final int depth,
                           final Map<String, Conflict<R, S>> conflicts,
                           final List<UnwindDetails<R, S>> unusedUnwindOptions) {
        super(name, requirements, activated, requirement, possibilities, depth, conflicts, unusedUnwindOptions);
    }

    /**
     * Removes a possibility from this state and return it.
     *
     * @return State that was popped off this state. The returned state contains a single possibility.
     */
    public PossibilityState<R, S> popPossibilityState() {
        final List<PossibilitySet<R, S>> possibilities = getPossibilities();
        final PossibilitySet<R, S> possibility = possibilities.isEmpty()
                                                 ? null
                                                 : possibilities.remove(possibilities.size() - 1);
        final PossibilityState<R, S> state = new PossibilityState<>(getName(),
                                                                    new ArrayList<>(getRequirements()),
                                                                    getActivated(),
                                                                    getRequirement().orElse(null),
                                                                    possibility,
                                                                    getDepth() + 1,
                                                                    new HashMap<>(getConflicts()),
                                                                    new ArrayList<>(getUnusedUnwindOptions()));
        state.getActivated().tag(state);
        return state;
    }
}
