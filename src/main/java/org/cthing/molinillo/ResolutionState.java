package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;


/**
 * Base class for resolution states.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class ResolutionState<R, S> {

    private String name;

    private List<R> requirements;

    private DependencyGraph<Payload<R, S>, R> activated;

    @Nullable
    private R requirement;

    private final List<PossibilitySet<R, S>> possibilities;

    private int depth;

    private Map<String, Conflict<R, S>> conflicts;

    private List<UnwindDetails<R, S>> unusedUnwindOptions;

    /**
     * Constructs an empty resolution state.
     */
    public ResolutionState() {
        this("", new ArrayList<>(), new DependencyGraph<>(), null, new ArrayList<>(), 0, new HashMap<>(),
             new ArrayList<>());
    }

    /**
     * Constructs a resolution state.
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
    public ResolutionState(final String name, final List<R> requirements,
                           final DependencyGraph<Payload<R, S>, R> activated, @Nullable final R requirement,
                           final List<PossibilitySet<R, S>> possibilities, final int depth,
                           final Map<String, Conflict<R, S>> conflicts,
                           final List<UnwindDetails<R, S>> unusedUnwindOptions) {
        this.name = name;
        this.requirements = requirements;
        this.activated = activated;
        this.requirement = requirement;
        this.possibilities = possibilities;
        this.depth = depth;
        this.conflicts = conflicts;
        this.unusedUnwindOptions = unusedUnwindOptions;
    }

    /**
     * Obtains the name of the current requirement.
     *
     * @return Name of the current requirement.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the current requirement.
     *
     * @param name Name of the current requirement
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Obtains the currently unsatisfied requirement.
     *
     * @return Currently unsatisfied requirement
     */
    public List<R> getRequirements() {
        return this.requirements;
    }

    /**
     * Sets the currently unsatisfied requirement.
     *
     * @param requirements  Currently unsatisfied requirement
     */
    public void setRequirements(final List<R> requirements) {
        this.requirements = requirements;
    }

    /**
     * Obtains the graph of activated dependencies.
     *
     * @return Graph of activated dependencies.
     */
    public DependencyGraph<Payload<R, S>, R> getActivated() {
        return this.activated;
    }

    /**
     * Sets the graph of activated dependencies.
     *
     * @param activated Graph of activated dependencies
     */
    public void setActivated(final DependencyGraph<Payload<R, S>, R> activated) {
        this.activated = activated;
    }

    /**
     * Obtains the current requirement.
     *
     * @return Current requirement
     */
    public Optional<R> getRequirement() {
        return Optional.ofNullable(this.requirement);
    }

    /**
     * Sets the current requirement.
     *
     * @param requirement Current requirement
     */
    public void setRequirement(@Nullable final R requirement) {
        this.requirement = requirement;
    }

    /**
     * Obtains the possibilities to satisfy the current requirement.
     *
     * @return Possibilities to satisfy the current requirement.
     */
    public List<PossibilitySet<R, S>> getPossibilities() {
        return this.possibilities;
    }

    /**
     * Obtains the depth of the resolution.
     *
     * @return Depth of the resolution.
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Sets the depth of the resolution.
     *
     * @param depth Depth of the resolution
     */
    public void setDepth(final int depth) {
        this.depth = depth;
    }

    /**
     * Obtains the unresolved conflicts, indexed by dependency name.
     *
     * @return Unresolved conflicts.
     */
    public Map<String, Conflict<R, S>> getConflicts() {
        return this.conflicts;
    }

    /**
     * Sets the unresolved conflicts, indexed by dependency name.
     *
     * @param conflicts Unresolved conflicts
     */
    public void setConflicts(final Map<String, Conflict<R, S>> conflicts) {
        this.conflicts = conflicts;
    }

    /**
     * Obtains the unwinds for previous conflicts that were not explored.
     *
     * @return Unwinds for previous conflicts.
     */
    public List<UnwindDetails<R, S>> getUnusedUnwindOptions() {
        return this.unusedUnwindOptions;
    }

    /**
     * Sets the unwinds for previous conflicts that were not explored.
     *
     * @param unusedUnwindOptions Unwinds for previous conflicts
     */
    public void setUnusedUnwindOptions(final List<UnwindDetails<R, S>> unusedUnwindOptions) {
        this.unusedUnwindOptions = unusedUnwindOptions;
    }
}
