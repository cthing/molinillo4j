package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * A conflict encountered during the resolution process.
 *
 * @param <R>               Type for a requirement
 * @param <S>               Type for a specification
 */
public final class Conflict<R, S> {

    private final R requirement;

    private final Map<?, Set<R>> requirements;

    @Nullable
    private final S existing;

    @Nullable
    private final PossibilitySet<R, S> possibilitySet;

    @Nullable
    private final R lockedRequirement;

    private final List<List<R>> requirementTrees;

    private final Map<String, S> activatedByName;

    @Nullable
    private final RuntimeException underlyingError;

    /**
     * Constructs a conflict.
     *
     * @param requirement Requirement that immediately led to the conflict
     * @param requirements Requirements that caused the conflict
     * @param existing Existing specification that was in conflict with the possibility
     * @param possibilitySet Set of specifications that were unable to be activated due to a conflict
     * @param lockedRequirement Locking requirement relevant to this conflict
     * @param requirementTrees Requirement trees that led to every requirement for the conflicting name
     * @param activatedByName Specification that are already activated
     * @param underlyingError Error that has occurred during resolution, and will be raised at the end of it if no
     *      resolution is found.
     */
    public Conflict(final R requirement, final Map<?, Set<R>> requirements, @Nullable final S existing,
                    @Nullable final PossibilitySet<R, S> possibilitySet, @Nullable final R lockedRequirement,
                    final List<List<R>> requirementTrees, final Map<String, S> activatedByName,
                    @Nullable final RuntimeException underlyingError) {
        this.requirement = requirement;
        this.requirements = requirements;
        this.existing = existing;
        this.possibilitySet = possibilitySet;
        this.lockedRequirement = lockedRequirement;
        this.requirementTrees = requirementTrees;
        this.activatedByName = activatedByName;
        this.underlyingError = underlyingError;
    }

    public R getRequirement() {
        return this.requirement;
    }

    public Map<?, Set<R>> getRequirements() {
        return this.requirements;
    }

    @Nullable
    public S getExisting() {
        return this.existing;
    }

    @Nullable
    public PossibilitySet<R, S> getPossibilitySet() {
        return this.possibilitySet;
    }

    @Nullable
    public R getLockedRequirement() {
        return this.lockedRequirement;
    }

    public List<List<R>> getRequirementTrees() {
        return this.requirementTrees;
    }

    public Map<String, S> getActivatedByName() {
        return this.activatedByName;
    }

    @Nullable
    public RuntimeException getUnderlyingError() {
        return this.underlyingError;
    }

    /**
     * Obtains the specification that could not be activated due to a conflict.
     *
     * @return Specification that could not be activated due to a conflict.
     */
    public Optional<S> getPossibility() {
        return this.possibilitySet == null ? Optional.empty() : this.possibilitySet.getLatestVersion();
    }

    @Override
    public String toString() {
        return "Conflict { requirement=" + this.requirement
                + ", requirements=" + this.requirements
                + ", existing=" + this.existing
                + ", possibilitySet=" + this.possibilitySet
                + ", lockedRequirement=" + this.lockedRequirement
                + ", requirementTrees=" + this.requirementTrees
                + ", activatedByName=" + this.activatedByName
                + ", underlyingError=" + this.underlyingError + " }";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final Conflict<?, ?> that = (Conflict<?, ?>)obj;
        return Objects.equals(this.requirement, that.requirement)
                && Objects.equals(this.requirements, that.requirements)
                && Objects.equals(this.existing, that.existing)
                && Objects.equals(this.possibilitySet, that.possibilitySet)
                && Objects.equals(this.lockedRequirement, that.lockedRequirement)
                && Objects.equals(this.requirementTrees, that.requirementTrees)
                && Objects.equals(this.activatedByName, that.activatedByName)
                && Objects.equals(this.underlyingError, that.underlyingError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.requirement, this.requirements, this.existing, this.possibilitySet,
                            this.lockedRequirement, this.requirementTrees, this.activatedByName,
                            this.underlyingError);
    }
}
