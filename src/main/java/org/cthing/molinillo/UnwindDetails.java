package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;


/**
 * Details of the state to unwind to when a conflict occurs, and the cause of the unwind.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class UnwindDetails<R, S> implements Comparable<UnwindDetails<R, S>> {

    private final int stateIndex;

    @Nullable
    private final R stateRequirement;

    private final List<R> requirementTree;

    private final List<R> conflictingRequirements;

    private final List<List<R>> requirementTrees;

    private final List<R> requirementsUnwoundToInstead;

    @Nullable
    private List<R> requirementsToAvoid;

    @Nullable
    private List<R> allRequirements;

    /**
     * Constructs the details of the unwind state.
     *
     * @param stateIndex Index of the state to unwind to
     * @param stateRequirement Requirement of the state being unwound to
     * @param requirementTree Requirement being relaxed
     * @param conflictingRequirements Requirements that combined to cause the conflict
     * @param requirementTrees Requirements for the conflict
     * @param requirementsUnwoundToInstead Unwind requirements that were chosen over this unwind
     */
    public UnwindDetails(final int stateIndex, @Nullable final R stateRequirement, final List<R> requirementTree,
                         final List<R> conflictingRequirements, final List<List<R>> requirementTrees,
                         final List<R> requirementsUnwoundToInstead) {
        this.stateIndex = stateIndex;
        this.stateRequirement = stateRequirement;
        this.requirementTree = requirementTree;
        this.conflictingRequirements = conflictingRequirements;
        this.requirementTrees = requirementTrees;
        this.requirementsUnwoundToInstead = requirementsUnwoundToInstead;
    }

    /**
     * Obtains the index of the state to unwind to.
     *
     * @return Index of the state to unwind to.
     */
    public int getStateIndex() {
        return this.stateIndex;
    }

    /**
     * Obtains the requirement of the state being unwound to.
     *
     * @return Requirement of the state being unwound to.
     */
    @Nullable
    public R getStateRequirement() {
        return this.stateRequirement;
    }

    /**
     * Obtains the requirement being relaxed.
     *
     * @return Requirement being relaxed.
     */
    public List<R> getRequirementTree() {
        return this.requirementTree;
    }

    /**
     * Obtains the requirements that combined to cause the conflict.
     *
     * @return Requirements that combined to cause the conflict.
     */
    public List<R> getConflictingRequirements() {
        return this.conflictingRequirements;
    }

    /**
     * Requirements for the conflict.
     *
     * @return Requirements for the conflict.
     */
    public List<List<R>> getRequirementTrees() {
        return this.requirementTrees;
    }

    /**
     * Obtains the unwind requirements that were chosen over this unwind.
     *
     * @return Unwind requirements that were chosen over this unwind.
     */
    public List<R> getRequirementsUnwoundToInstead() {
        return this.requirementsUnwoundToInstead;
    }

    /**
     * Obtains the index of the state requirement in the reversed requirement tree (the conflicting requirement
     * itself will be at location 0).
     *
     * @return Index of the state requirement.
     */
    public int reversedRequirementTreeIndex() {
        if (this.stateRequirement != null) {
            final List<R> reversedRequirementTree = new ArrayList<>(this.requirementTree);
            Collections.reverse(reversedRequirementTree);
            final int index = reversedRequirementTree.indexOf(this.stateRequirement);
            if (index != -1) {
                return index;
            }
        }
        return 999_999;
    }

    /**
     * Indicates whether the requirement of the state being unwound to directly caused the conflict. Note that
     * in this case, it is impossible for the state being unwound to, to be a parent of any other conflicting
     * requirements (or there would be circularity).
     *
     * @return Indicates whether the requirement of the state directly cause the conflict.
     */
    public boolean unwindingToPrimaryRequirement() {
        return this.requirementTree.get(this.requirementTree.size() - 1).equals(this.stateRequirement);
    }

    /**
     * Obtains the sub-dependencies to avoid when choosing a new possibility for the state being unwound to. Only
     * relevant for non-primary unwinds.
     *
     * @return Sub-dependencies to avoid when choosing a new possibility.
     */
    public List<R> subDependenciesToAvoid() {
        if (this.requirementsToAvoid == null) {
            this.requirementsToAvoid = new ArrayList<>();
            for (final List<R> tree : this.requirementTrees) {
                final int index = tree.indexOf(this.stateRequirement);
                if (index != -1 && index < tree.size() - 1) {
                    this.requirementsToAvoid.add(tree.get(index + 1));
                }
            }
            this.requirementsToAvoid.removeIf(Objects::isNull);
        }
        return this.requirementsToAvoid;
    }

    /**
     * Obtains all the requirements that led to the need for this unwind.
     *
     * @return All requirements that led to the need for this unwind.
     */
    public List<R> allRequirements() {
        if (this.allRequirements == null) {
            this.allRequirements = this.requirementTrees.stream()
                                                        .flatMap(List::stream) // Flatten the list of lists
                                                        .collect(Collectors.toList());
        }
        return this.allRequirements;
    }

    @Override
    public String toString() {
        return "UnwindDetails { stateIndex=" + this.stateIndex
                + ", stateRequirement=" + this.stateRequirement
                + ", requirementTree=" + this.requirementTree
                + ", conflictingRequirements=" + this.conflictingRequirements
                + ", requirementTrees=" + this.requirementTrees
                + ", requirementsUnwoundToInstead=" + this.requirementsUnwoundToInstead
                + " }";
    }

    /**
     * Compare UnwindDetails when choosing which state to unwind to. If two options have the same stateIndex,
     * prefer the one most removed from a requirement that caused the conflict. Both options would unwind to
     * the same state, but a `grandparent` option will filter out fewer of its possibilities after doing so -
     * where a state is both a `parent` and a `grandparent` to requirements that have caused a conflict this
     * is the correct behaviour.
     *
     * @param other UnwindDetails to be compared
     * @return Ordering integer
     */
    @Override
    public int compareTo(final UnwindDetails<R, S> other) {
        if (this.stateIndex > other.stateIndex) {
            return 1;
        }
        if (this.stateIndex == other.stateIndex) {
            return Integer.compare(this.reversedRequirementTreeIndex(), other.reversedRequirementTreeIndex());
        }
        return -1;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final UnwindDetails<?, ?> that = (UnwindDetails<?, ?>)obj;
        return this.stateIndex == that.stateIndex
                && this.reversedRequirementTreeIndex() == that.reversedRequirementTreeIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stateIndex, this.reversedRequirementTreeIndex());
    }
}
