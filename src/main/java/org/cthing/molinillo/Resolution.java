package org.cthing.molinillo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.cthing.molinillo.errors.CircularDependencyError;
import org.cthing.molinillo.errors.NoSuchDependencyError;
import org.cthing.molinillo.errors.ResolverError;
import org.cthing.molinillo.errors.VersionConflictError;
import org.cthing.molinillo.graph.Edge;
import org.cthing.molinillo.graph.Vertex;


/**
 * Performs the dependency resolution.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class Resolution<R, S> {

    private static final String SWAP = "SWAP";
    private static final String INITIAL_STATE = "INITIAL_STATE";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss:SSSZ";

    private final SpecificationProvider<R, S> specificationProvider;
    private final UI resolverUi;
    private final Set<R> originalRequested;
    private final DependencyGraph<R, R> base;

    private int iterationRate;
    private int iterationCount;
    private long startedAt;
    private final List<ResolutionState<R, S>> states;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<R, List<Integer>> parentsOf;

    /**
     * Constructs a resolution engine.
     *
     * @param specificationProvider Provider for dependencies, requirements, specifications, versions, etc.
     * @param resolverUi Provides feedback to the user on the resolution process
     * @param originalRequested Dependencies that are explicitly required
     * @param base Dependency graph to which dependencies should be locked
     */
    public Resolution(final SpecificationProvider<R, S> specificationProvider, final UI resolverUi,
                      final Set<R> originalRequested, final DependencyGraph<R, R> base) {
        this.specificationProvider = specificationProvider;
        this.resolverUi = resolverUi;
        this.base = base;
        this.originalRequested = originalRequested;

        this.states = new ArrayList<>();
        this.parentsOf = new HashMap<>();
    }

    /**
     * Obtains the provider for dependencies, requirements, specifications, versions, etc.
     *
     * @return Provider for all metadata.
     */
    public SpecificationProvider<R, S> getSpecificationProvider() {
        return this.specificationProvider;
    }

    /**
     * Obtains the object that provides feedback to the user on the resolution process.
     *
     * @return Provides feedback to users.
     */
    public UI getResolverUi() {
        return this.resolverUi;
    }

    /**
     * Obtains the dependencies that are explicitly required.
     *
     * @return Direct dependencies.
     */
    public Set<R> getOriginalRequested() {
        return this.originalRequested;
    }

    /**
     * Obtains the dependency graph to which dependencies should be locked.
     *
     * @return Locking dependency graph.
     */
    public DependencyGraph<R, R> getBase() {
        return this.base;
    }

    /**
     * Resolves the originally requested dependencies into a full dependency graph.
     *
     * @return Dependency graph of the successfully resolved dependencies.
     * @throws ResolverError if a problem was encountered during the resolution process.
     */
    public DependencyGraph<Payload<R, S>, R> resolve() throws ResolverError {
        startResolution();

        try {
            ResolutionState<R, S> state = getState();
            while (state != null) {
                if (state.getRequirement() == null && state.getRequirements().isEmpty()) {
                    break;
                }

                indicateProgress();

                if (state instanceof final DependencyState<R, S> dependencyState) {
                    debug(getDepth(), "Creating possibility state for %s (%d remaining)", getRequirement(),
                          getPossibilities().size());
                    final PossibilityState<R, S> possibilityState = dependencyState.popPossibilityState();
                    this.states.add(possibilityState);
                    getActivated().tag(possibilityState);
                }

                processTopmostState();
                state = getState();
            }

            return resolveActivatedSpecs();
        } finally {
            endResolution();
        }
    }

    /**
     * See {@link ResolutionState#getName()}.
     */
    private String getName() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getName();
    }

    /**
     * See {@link ResolutionState#getRequirements()}.
     */
    private List<R> getRequirements() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getRequirements();
    }

    /**
     * See {@link ResolutionState#getActivated()}.
     */
    private DependencyGraph<Payload<R, S>, R> getActivated() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getActivated();
    }

    /**
     * See {@link ResolutionState#getRequirement()}.
     */
    @Nullable
    private R getRequirement() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getRequirement();
    }

    /**
     * See {@link ResolutionState#getPossibilities()}.
     */
    private List<PossibilitySet<R, S>> getPossibilities() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getPossibilities();
    }

    /**
     * See {@link ResolutionState#getDepth()}.
     */
    private int getDepth() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getDepth();
    }

    /**
     * See {@link ResolutionState#getConflicts()}.
     */
    private Map<String, Conflict<R, S>> getConflicts() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getConflicts();
    }

    /**
     * See {@link ResolutionState#getUnusedUnwindOptions()}.
     */
    private List<UnwindDetails<R, S>> getUnusedUnwindOptions() {
        final ResolutionState<R, S> currentState = getState();
        return ((currentState == null) ? new ResolutionState<R, S>() : currentState).getUnusedUnwindOptions();
    }

    /**
     * See {@link SpecificationProvider#searchFor(R)}.
     */
    private List<S> searchFor(final R dependency) {
        try {
            return this.specificationProvider.searchFor(dependency);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#dependenciesFor(Object)}.
     */
    private Set<R> dependenciesFor(final S specification) {
        try {
            return this.specificationProvider.dependenciesFor(specification);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#requirementSatisfiedBy(Object, DependencyGraph, Object)}.
     */
    private boolean requirementSatisfiedBy(final R requirement, final DependencyGraph<Payload<R, S>, R> activated,
                                           final S specification) {
        try {
            return this.specificationProvider.requirementSatisfiedBy(requirement, activated, specification);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#nameForDependency(Object)}.
     */
    private String nameForDependency(final R dependency) {
        try {
            return this.specificationProvider.nameForDependency(dependency);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#nameForSpecification(Object)}.
     */
    private String nameForSpecification(final S specification) {
        try {
            return this.specificationProvider.nameForSpecification(specification);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#nameForExplicitDependencySource()}.
     */
    private String nameForExplicitDependencySource() {
        try {
            return this.specificationProvider.nameForExplicitDependencySource();
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#nameForLockingDependencySource()}.
     */
    private String nameForLockingDependencySource() {
        try {
            return this.specificationProvider.nameForLockingDependencySource();
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#sortDependencies(List, DependencyGraph, Map)}.
     */
    private List<R> sortDependencies(final List<R> dependencies, final DependencyGraph<Payload<R, S>, R> activated,
                                     final Map<String, Conflict<R, S>> conflicts) {
        try {
            return this.specificationProvider.sortDependencies(dependencies, activated, conflicts);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * See {@link SpecificationProvider#allowMissing(Object)}.
     */
    private boolean allowMissing(final R dependency) {
        try {
            return this.specificationProvider.allowMissing(dependency);
        } catch (final NoSuchDependencyError ex) {
            throw processNoSuchDependencyError(ex);
        }
    }

    /**
     * Processes a {@link NoSuchDependencyError} to augment it with the list of dependents of the dependency that
     * could not be found.
     *
     * @param error Error whose dependents are added
     * @return Original error with dependents added
     */
    private NoSuchDependencyError processNoSuchDependencyError(final NoSuchDependencyError error) {
        if (getState() != null) {
            final Vertex<Payload<R, S>, R> vertex = getActivated().vertexNamed(nameForDependency(error.getDependency()));
            if (vertex != null) {
                error.getRequiredBy().addAll(vertex.getIncomingEdges()
                                                   .stream()
                                                   .map(e -> e.getOrigin().getName())
                                                   .toList());
                if (!vertex.getExplicitRequirements().isEmpty()) {
                    error.getRequiredBy().add(nameForExplicitDependencySource());
                }
            }
        }

        return error;
    }

    /**
     * Sets up the resolution process.
     */
    private void startResolution() {
        this.startedAt = System.currentTimeMillis();

        pushInitialState();

        debug(0, "Starting resolution (%s)\nUser-requested dependencies: %s",
              new SimpleDateFormat(DATE_FORMAT).format(new Date(this.startedAt)), this.originalRequested);

        this.resolverUi.beforeResolution();
    }

    private DependencyGraph<Payload<R, S>, R> resolveActivatedSpecs() {
        for (final Map.Entry<String, Vertex<Payload<R, S>, R>> entry : getActivated().getVertices().entrySet()) {
            final Vertex<Payload<R, S>, R> vertex = entry.getValue();
            if (vertex.getPayload().isPresent()) {
                final PossibilitySet<R, S> possibilitySet = vertex.getPayload().get().getPossibilitySet();
                final List<S> possibilities = new ArrayList<>(possibilitySet.getPossibilities());
                Collections.reverse(possibilities);
                final S latestVersion =
                        possibilities.stream()
                                     .filter(possibility -> vertex.requirements()
                                                                  .stream()
                                                                  .allMatch(requirement -> requirementSatisfiedBy(requirement,
                                                                                                                  getActivated(),
                                                                                                                  possibility)))
                                     .findFirst()
                                     .orElseThrow();
                getActivated().setPayload(vertex.getName(), new Payload<>(latestVersion));
            }
        }

        return getActivated();
    }

    /**
     * Ends the resolution process.
     */
    private void endResolution() {
        final long endedAt = System.currentTimeMillis();

        this.resolverUi.afterResolution();

        debug(0, "Finished resolution (%d steps)", this.iterationCount);
        debug(0, "                    (Took %d ms)", endedAt - this.startedAt);
        debug(0, "                    (%s)", new SimpleDateFormat(DATE_FORMAT).format(new Date(endedAt)));

        final ResolutionState<R, S> state = getState();
        if (state != null) {
            final Function<Boolean, String> payloads =
                    empty -> getActivated().getVertices()
                                           .values()
                                           .stream()
                                           .filter(vertex -> empty == vertex.getPayload().isEmpty())
                                           .map(vertex -> vertex.getPayload().orElseThrow().toString())
                                           .collect(Collectors.joining(", "));

            debug(0, "Unactivated: %s", payloads.apply(Boolean.TRUE));
            debug(0, "Activated: %s", payloads.apply(Boolean.FALSE));
        }
    }

    /**
     * Processes the topmost available requirement state on the stack.
     */
    private void processTopmostState() {
        try {
            final List<PossibilitySet<R, S>> possibilities = getPossibilities();
            if (!possibilities.isEmpty() && possibilities.get(possibilities.size() - 1) != null) {
                attemptToActivate();
            } else {
                createConflict(null);
                unwindForConflict();
            }
        } catch (final CircularDependencyError ex) {
            createConflict(ex);
            unwindForConflict();
        }
    }

    /**
     * Obtains the current possibility which the resolution is trying to activate.
     *
     * @return Current possibility.
     */
    private PossibilitySet<R, S> getPossibility() {
        final List<PossibilitySet<R, S>> possibilities = getPossibilities();
        return possibilities.get(possibilities.size() - 1);
    }

    /**
     * Obtains the current state on which the resolution is operating.
     *
     * @return Current resolution state.
     */
    @Nullable
    private ResolutionState<R, S> getState() {
        return this.states.isEmpty() ? null : this.states.get(this.states.size() - 1);
    }

    /**
     * Creates and pushes the initial state for the resolution based upon the requested dependencies.
     */
    private void pushInitialState() {
        final DependencyGraph<Payload<R, S>, R> graph = new DependencyGraph<>();

        for (final R requested : this.originalRequested) {
            final Vertex<Payload<R, S>, R> vertex = graph.addVertex(nameForDependency(requested), null, true);
            vertex.getExplicitRequirements().add(requested);
        }

        graph.tag(INITIAL_STATE);
        pushStateForRequirements(this.originalRequested, true, graph);
    }

    /**
     * Unwinds the states stack because a conflict has been encountered.
     */
    private void unwindForConflict() {
        final UnwindDetails<R, S> detailsForUnwind = buildDetailsForUnwind();
        final List<UnwindDetails<R, S>> unwindOptions = new ArrayList<>(getUnusedUnwindOptions());
        debug(getDepth(), "Unwinding for conflict: %s to %d", getRequirement(), detailsForUnwind.getStateIndex() / 2);

        final Map<String, Conflict<R, S>> c = new HashMap<>(getConflicts());
        final List<ResolutionState<R, S>> statesToSlice = this.states.subList(detailsForUnwind.getStateIndex() + 1,
                                                                              this.states.size());
        final List<ResolutionState<R, S>> slicedStates = new ArrayList<>(statesToSlice);
        statesToSlice.clear();
        raiseErrorUnlessState(c);

        if (!slicedStates.isEmpty()) {
            getActivated().rewindTo(slicedStates.get(0));
        } else {
            getActivated().rewindTo(INITIAL_STATE);
        }

        final ResolutionState<R, S> state = getState();
        assert state != null;
        state.setConflicts(c);
        state.setUnusedUnwindOptions(unwindOptions);

        filterPossibilitiesAfterUnwind(detailsForUnwind);

        final int index = this.states.size() - 1;

        for (final Map.Entry<R, List<Integer>> entry : this.parentsOf.entrySet()) {
            entry.getValue().removeIf(i -> i >= index);
        }

        state.getUnusedUnwindOptions().removeIf(uw -> uw.getStateIndex() >= index);
    }

    /**
     * Raises a {@link VersionConflictError}, or any underlying error, if there is no current state.
     *
     * @param conflicts Conflicts for the error
     */
    private void raiseErrorUnlessState(final Map<String, Conflict<R, S>> conflicts) {
        if (getState() == null) {
            final Optional<RuntimeException> underlyingError = conflicts.values()
                                                                        .stream()
                                                                        .map(Conflict::getUnderlyingError)
                                                                        .filter(Objects::nonNull)
                                                                        .findFirst();

            if (underlyingError.isPresent()) {
                throw underlyingError.get();
            } else {
                throw new VersionConflictError(conflicts, getSpecificationProvider());
            }
        }
    }

    /**
     * Provides the details of the nearest index which the resolution process could be unwound.
     *
     * @return Details of the unwind
     */
    private UnwindDetails<R, S> buildDetailsForUnwind() {
        // Get the possible unwinds for the current conflict.
        final Conflict<R, S> currentConflict = getConflicts().get(getName());
        final List<R> bindingRequirements = bindingRequirementsForConflict(currentConflict);
        final List<UnwindDetails<R, S>> unwindDetails = unwindOptionsForRequirements(bindingRequirements);

        final UnwindDetails<R, S> lastDetailForCurrentUnwind = Collections.max(unwindDetails);

        class CurrentDetailHolder {
            UnwindDetails<R, S> currentDetail;

            CurrentDetailHolder(final UnwindDetails<R, S> currentDetail) {
                this.currentDetail = currentDetail;
            }
        }
        final CurrentDetailHolder currentDetailHolder = new CurrentDetailHolder(lastDetailForCurrentUnwind);

        final List<R> allRequirements = lastDetailForCurrentUnwind.allRequirements();
        final int allRequirementsSize = allRequirements.size();

        // Look for past conflicts that could be unwound to affect the requirement tree for the current conflict.
        final List<UnwindDetails<R, S>> relevantUnusedUnwinds =
                getUnusedUnwindOptions().stream()
                                        .filter(alternative -> {
                                            final List<R> diffReqs = new ArrayList<>(allRequirements);
                                            diffReqs.removeAll(alternative.getRequirementsUnwoundToInstead());
                                            if (diffReqs.size() == allRequirementsSize) {
                                                return false;
                                            }
                                            currentDetailHolder.currentDetail =
                                                    alternative.compareTo(currentDetailHolder.currentDetail) > 0
                                                    ? alternative
                                                    : currentDetailHolder.currentDetail;
                                            return true;
                                        })
                                        .toList();

        // Add the current unwind options to the collection of unused unwind options. The "used" option will be
        // filtered out during "unwindForConflict".
        final ResolutionState<R, S> state = getState();
        assert state != null;
        state.getUnusedUnwindOptions().addAll(unwindDetails.stream()
                                                           .filter(detail -> detail.getStateIndex() != -1)
                                                           .toList());

        // Update the "requirementUnwoundToInstead" on any relevant unused unwinds.
        for (final UnwindDetails<R, S> d : relevantUnusedUnwinds) {
            @Nullable final R req = currentDetailHolder.currentDetail.getStateRequirement();
            if (!d.getRequirementsUnwoundToInstead().contains(req)) {
                d.getRequirementsUnwoundToInstead().add(req);
            }
        }

        unwindDetails.forEach(d -> {
            @Nullable final R req = currentDetailHolder.currentDetail.getStateRequirement();
            if (!d.getRequirementsUnwoundToInstead().contains(req)) {
                d.getRequirementsUnwoundToInstead().add(req);
            }
        });

        return currentDetailHolder.currentDetail;
    }

    /**
     * Creates a list of {@link UnwindDetails} that have a change of resolving the specified requirements.
     *
     * @param bindingRequirements Requirements that combine to create a conflict
     * @return Unwind details that might resolve the specified requirements.
     */
    private List<UnwindDetails<R, S>> unwindOptionsForRequirements(final List<R> bindingRequirements) {
        final List<UnwindDetails<R, S>> unwindDetails = new ArrayList<>();
        final List<List<R>> trees = new ArrayList<>();

        final List<R> reversedBindingRequirements = new ArrayList<>(bindingRequirements);
        Collections.reverse(reversedBindingRequirements);

        for (final R r : reversedBindingRequirements) {
            final List<R> partialTree = new ArrayList<>();
            partialTree.add(r);
            trees.add(partialTree);
            unwindDetails.add(new UnwindDetails<>(-1, null, partialTree, bindingRequirements, trees,
                                                  new ArrayList<>()));

            // If this requirement has alternative possibilities, check if any would satisfy the other requirements
            // that created this conflict
            ResolutionState<R, S> requirementState = findStateFor(r);

            if (conflictFixingPossibilities(requirementState, bindingRequirements)) {
                unwindDetails.add(new UnwindDetails<>(this.states.indexOf(requirementState), r, partialTree,
                                                      bindingRequirements, trees, new ArrayList<>()));
            }

            // Next, look at the parent of this requirement, and check if the requirement could have been avoided
            // if an alternative PossibilitySet had been chosen
            @Nullable R parentR = parentOf(r);
            if (parentR != null) {
                partialTree.add(0, parentR);
                requirementState = findStateFor(parentR);
                assert requirementState != null;
                if (requirementState.getPossibilities().stream().anyMatch(set -> !set.getDependencies().contains(r))) {
                    unwindDetails.add(new UnwindDetails<>(this.states.indexOf(requirementState), parentR,
                                                          partialTree, bindingRequirements, trees, new ArrayList<>()));
                }

                // Finally, look at the grandparent and up of this requirement, looking for any possibilities that
                // wouldn't create their parent requirement
                @Nullable R grandparentR = parentOf(parentR);
                while (grandparentR != null) {
                    partialTree.add(0, grandparentR);
                    requirementState = findStateFor(grandparentR);
                    assert requirementState != null;
                    if (requirementState.getPossibilities().stream().anyMatch(set -> !set.getDependencies().contains(r))) {
                        unwindDetails.add(new UnwindDetails<>(this.states.indexOf(requirementState),
                                                              grandparentR, partialTree, bindingRequirements, trees,
                                                              new ArrayList<>()));
                    }

                    parentR = grandparentR;
                    grandparentR = parentOf(parentR);
                }
            }
        }

        return unwindDetails;
    }

    /**
     * Indicates if the specified state has any possibilities that could satisfy the specified requirements.
     *
     * @param state State whose possibilities are to be tested
     * @param bindingRequirements Requirements to test
     * @return {@code true} if the specified state has any possibilities that could satisfy the specified requirements.
     */
    private boolean conflictFixingPossibilities(@Nullable final ResolutionState<R, S> state,
                                                final List<R> bindingRequirements) {
        if (state == null) {
            return false;
        }

        return state.getPossibilities()
                    .stream()
                    .anyMatch(possibilitySet -> possibilitySet.getPossibilities()
                                                              .stream()
                                                              .anyMatch(poss -> possibilitySatisfiesRequirements(poss, bindingRequirements))
        );
    }


    /**
     * Filters a state's possibilities to remove any that would not fix the conflict just unwound from.
     *
     * @param unwindDetails Details of the conflict just unwound from
     */
    private void filterPossibilitiesAfterUnwind(final UnwindDetails<R, S> unwindDetails) {
        if (getState() == null || getState().getPossibilities().isEmpty()) {
            return;
        }

        if (unwindDetails.unwindingToPrimaryRequirement()) {
            filterPossibilitiesForPrimaryUnwind(unwindDetails);
        } else {
            filterPossibilitiesForParentUnwind(unwindDetails);
        }
    }

    /**
     * Filters a state's possibilities to remove any that would not satisfy the requirements in the conflict that
     * was just unwound from.
     *
     * @param unwindDetails Details of the conflict just unwound from
     */
    @SuppressWarnings("CodeBlock2Expr")
    private void filterPossibilitiesForPrimaryUnwind(final UnwindDetails<R, S> unwindDetails) {
        final List<UnwindDetails<R, S>> unwindsToState =
                getUnusedUnwindOptions().stream()
                                        .filter(uw -> uw.getStateIndex() == unwindDetails.getStateIndex())
                                        .collect(Collectors.toList());
        unwindsToState.add(unwindDetails);

        final List<List<R>> unwindRequirementSets = unwindsToState.stream()
                                                                  .map(UnwindDetails::getConflictingRequirements)
                                                                  .toList();

        final ResolutionState<R, S> state = getState();
        assert state != null;
        state.getPossibilities().removeIf(possibilitySet -> {
            return possibilitySet.getPossibilities().stream().noneMatch(poss -> {
                return unwindRequirementSets.stream().anyMatch(requirements -> {
                    return possibilitySatisfiesRequirements(poss, requirements);
                });
            });
        });
    }

    /**
     * Filters a state's possibilities to remove any that would (eventually) create a requirement in the conflict
     * just unwound from.
     *
     * @param unwindDetails Details of the conflict just unwound from
     */
    private void filterPossibilitiesForParentUnwind(final UnwindDetails<R, S> unwindDetails) {
        final List<UnwindDetails<R, S>> unwindsToState =
                getUnusedUnwindOptions().stream()
                                        .filter(uw -> uw.getStateIndex() == unwindDetails.getStateIndex())
                                        .collect(Collectors.toList());
        unwindsToState.add(unwindDetails);

        final List<UnwindDetails<R, S>> primaryUnwinds =
                unwindsToState.stream()
                              .filter(UnwindDetails::unwindingToPrimaryRequirement)
                              .distinct()
                              .toList();
        final List<UnwindDetails<R, S>> parentUnwinds =
                unwindsToState.stream()
                              .distinct()
                              .filter(uw -> !primaryUnwinds.contains(uw))
                              .toList();

        final List<PossibilitySet<R, S>> allowedPossibilitySets =
                primaryUnwinds.stream()
                              .flatMap(unwind ->
                                  this.states.get(unwind.getStateIndex())
                                             .getPossibilities()
                                             .stream()
                                             .filter(possibilitySet ->
                                                         possibilitySet.getPossibilities()
                                                                       .stream()
                                                                       .anyMatch(poss -> possibilitySatisfiesRequirements(poss, unwind.getConflictingRequirements()))
                                             )
                              )
                              .toList();

        final List<R> requirementsToAvoid = parentUnwinds.stream()
                                                         .flatMap(uw -> uw.subDependenciesToAvoid().stream())
                                                         .toList();

        final ResolutionState<R, S> state = getState();
        assert state != null;
        state.getPossibilities()
             .removeIf(possibilitySet -> !allowedPossibilitySets.contains(possibilitySet)
                     && possibilitySet.getDependencies().containsAll(requirementsToAvoid));
    }


    /**
     * Indicates whether the specified possibility satisfies all the specified requirements.
     *
     * @param possibility Possibility to test
     * @param requirements Requirements that the specified possibility may satisfy
     * @return {@code true} if the specified possibility satisfies all the specified requirements.
     * @throws NoSuchDependencyError if an error has occurred
     */
    private boolean possibilitySatisfiesRequirements(final S possibility, final Collection<R> requirements) {
        final String name = nameForSpecification(possibility);

        getActivated().tag(SWAP);

        if (getActivated().vertexNamed(name) != null) {
            final Payload<R, S> payload = new Payload<>(possibility);
            getActivated().setPayload(name, payload);
        }
        final boolean satisfied = requirements.stream()
                                              .allMatch(requirement -> requirementSatisfiedBy(requirement,
                                                                                              getActivated(),
                                                                                              possibility));

        getActivated().rewindTo(SWAP);

        return satisfied;
    }

    /**
     * Creates a minimal list of requirements that would cause the specified conflict to occur.
     *
     * @param conflict Conflict whose requirements are desired
     * @return Minimal list of requirements that would cause the specified conflict.
     */
    private List<R> bindingRequirementsForConflict(final Conflict<R, S> conflict) {
        final List<R> result = new ArrayList<>();
        if (conflict.getPossibility() == null) {
            result.add(conflict.getRequirement());
            return result;
        }

        final List<R> possibleBindingRequirements = conflict.getRequirements().values()
                                                            .stream()
                                                            .flatMap(Collection::stream)
                                                            .distinct()
                                                            .collect(Collectors.toList());

        // When there is a CircularDependencyError, the conflicting requirement (i.e. the one causing the cycle)
        // will not be `conflict.requirement` (which will not be for the right state, because it will not have been
        // created, because it is circular). Make sure that requirement is in the conflict’s list, or it will not be
        // possible to unwind properly. In this case, just return all the requirements for the conflict.
        if (conflict.getUnderlyingError() != null) {
            return possibleBindingRequirements;
        }

        final List<S> possibilities = searchFor(conflict.getRequirement());

        if (bindingRequirementInSet(null, possibleBindingRequirements, possibilities)) {
            // If all the requirements together do not filter out all possibilities, then the only two requirements needed
            // to be considered are the initial one (where the dependency's version was first chosen) and the last one.
            result.add(conflict.getRequirement());
            result.add(requirementForExistingName(nameForDependency(conflict.getRequirement())));
            result.removeIf(Objects::isNull);
        } else {
            // Loop through the possible binding requirements, removing each one that does not bind. Iterate in reverse
            // because we want the earliest set of binding requirements, and refine the array on each iteration.
            final List<R> bindingRequirements = new ArrayList<>(possibleBindingRequirements);
            final List<R> toRemove =
                    bindingRequirements.stream()
                                       .filter(req -> !req.equals(conflict.getRequirement())
                                               && !bindingRequirementInSet(req, bindingRequirements, possibilities))
                                       .toList();

            bindingRequirements.removeAll(toRemove);
            result.addAll(bindingRequirements);
        }

        return result;
    }

    /**
     * Indicates if the specified requirement is required to filter out all elements of the specified possibilities.
     *
     * @param requirement Requirement to test
     * @param possibleBindingRequirements Requirements
     * @param possibilities Possibilities the requirements will be used to filter
     * @return {@code true} if the specified requirement is required to filter out all elements of the specified
     *      possibilities.
     */
    private boolean bindingRequirementInSet(@Nullable final R requirement, final List<R> possibleBindingRequirements,
                                            final Collection<S> possibilities) {
        return possibilities.stream()
                            .anyMatch(poss -> possibilitySatisfiesRequirements(poss, possibleBindingRequirements
                                    .stream()
                                    .filter(req -> !req.equals(requirement))
                                    .collect(Collectors.toList())
        ));
    }

    /**
     * Obtains the requirements that led to the specified requirement being added to the list of requirements.
     *
     * @param requirement Requirements whose parent is desired
     * @return Requirement which led to the specified requirement being added to the list of requirements, or
     *      {@code null} if no such requirement could be found.
     */
    @Nullable
    private R parentOf(@Nullable final R requirement) {
        if (requirement == null) {
            return null;
        }

        final List<Integer> parents = this.parentsOf.computeIfAbsent(requirement, key -> new ArrayList<>());
        if (parents.isEmpty()) {
            return null;
        }

        final int index = parents.get(parents.size() - 1);
        if (index < 0 || index >= this.states.size()) {
            return null;
        }
        final ResolutionState<R, S> resolutionState = this.states.get(index);
        if (resolutionState == null) {
            return null;
        }

        return resolutionState.getRequirement();
    }

    /**
     * Finds the requirement that led to a version of a possibility with the given name being activated.
     *
     * @param name Name of the possibility
     * @return Requirement that led to a version of the named possibility being activated. Returns {@code null}
     *      if the requirement could not be found.
     */
    @Nullable
    private R requirementForExistingName(final String name) {
        final Vertex<Payload<R, S>, R> vertex = getActivated().vertexNamed(name);
        if (vertex == null || vertex.getPayload().isEmpty()) {
            return null;
        }

        return this.states.stream()
                          .filter(state -> state.getName().equals(name))
                          .findFirst()
                          .map(ResolutionState::getRequirement)
                          .orElse(null);

    }

    /**
     * Finds the state whose requirement is the specified requirement.
     *
     * @param requirement Requirement for the state that is to be found
     * @return State with the specified requirement or {@code null} if either the specified requirement is
     *      {@code null} or a state with the specified requirement could not be found.
     */
    @Nullable
    private ResolutionState<R, S> findStateFor(@Nullable final R requirement) {
        if (requirement == null) {
            return null;
        }

        return this.states.stream()
                          .filter(state -> requirement.equals(state.getRequirement()))
                          .findFirst()
                          .orElse(null);
    }

    /**
     * Creates a conflict object representing the failure to activate the possibility in conjunction with the
     * current state.
     *
     * @param underlyingError Error causing the conflict
     * @return Newly constructed conflict object.
     */
    private Conflict<R, S> createConflict(@Nullable final RuntimeException underlyingError) {
        final Vertex<Payload<R, S>, R> vertex = getActivated().vertexNamed(getName());
        assert vertex != null;
        @Nullable final R lockedRequirement = lockedRequirementNamed(getName());

        final Map<Object, Set<R>> requirements = new HashMap<>();
        if (!vertex.getExplicitRequirements().isEmpty()) {
            requirements.put(nameForExplicitDependencySource(), vertex.getExplicitRequirements());
        }
        if (lockedRequirement != null) {
            final Set<R> lockedRequirements = new LinkedHashSet<>();
            lockedRequirements.add(lockedRequirement);
            requirements.put(nameForLockingDependencySource(), lockedRequirements);
        }
        for (final Edge<Payload<R, S>, R> edge : vertex.getIncomingEdges()) {
            final PossibilitySet<R, S> possibilitySet = edge.getOrigin().getPayload().orElseThrow().getPossibilitySet();
            @Nullable final S latestVersion = possibilitySet.getLatestVersion();

            final Set<R> reqs = requirements.get(latestVersion);
            final Set<R> newReqs = new LinkedHashSet<>();
            newReqs.add(edge.getRequirement());
            if (reqs != null) {
                newReqs.addAll(reqs);
            }
            requirements.put(latestVersion, newReqs);
        }

        final Map<String, S> activatedByName = new HashMap<>();
        for (final Vertex<Payload<R, S>, R> v : getActivated().getVertices().values()) {
            if (v.getPayload().isPresent()) {
                activatedByName.put(v.getName(), v.getPayload().get().getPossibilitySet().getLatestVersion());
            }
        }

        @Nullable final R requirement = getRequirement();
        assert requirement != null;
        final Optional<Payload<R, S>> payload = vertex.getPayload();
        @Nullable final S existingSpecification = payload.map(rsPayload -> rsPayload.getPossibilitySet().getLatestVersion())
                                                         .orElse(null);
        @Nullable final PossibilitySet<R, S> possibilitySet = getPossibilities().isEmpty()
                                                              ? null
                                                              : getPossibilities().get(getPossibilities().size() - 1);
        final Conflict<R, S> conflict = new Conflict<>(requirement,
                                                       requirements,
                                                       existingSpecification,
                                                       possibilitySet,
                                                       lockedRequirement,
                                                       requirementTrees(),
                                                       activatedByName,
                                                       underlyingError);
        getConflicts().put(getName(), conflict);
        return conflict;
    }

    /**
     * Provides the requirement trees that led to every requirement for the current specification.
     *
     * @return Requirement trees that led to every requirement for the current specification.
     */
    private List<List<R>> requirementTrees() {
        final Vertex<Payload<R, S>, R> vertex = getActivated().vertexNamed(getName());
        assert vertex != null;

        return vertex.requirements()
                     .stream()
                     .map(this::requirementTreeFor)
                     .collect(Collectors.toList());
    }

    /**
     * Provides a list of requirements that led to the specified requirement being required.
     *
     * @param requirement Requirement whose causes are to be obtained
     * @return Requirements which led to the specified requirement being required
     */
    private List<R> requirementTreeFor(final R requirement) {
        final List<R> tree = new ArrayList<>();

        @Nullable R req = requirement;
        while (req != null) {
            tree.add(0, req);
            req = parentOf(req);
        }

        return tree;
    }

    /**
     * Outputs a resolution progress indication at the approximate time interval specified by
     * {@link UI#getProgressRate()}.
     */
    private void indicateProgress() {
        this.iterationCount++;

        // One time calibration of the number of iterations that can be performed within the desired output time
        // interval.
        if (this.iterationRate == 0) {
            if ((System.currentTimeMillis() - this.startedAt) >= this.resolverUi.getProgressRate()) {
                this.iterationRate = this.iterationCount;
            }
        }

        // If the expected number of iterations have been performed, output a progress marker.
        if (this.iterationRate > 0 && (this.iterationCount % this.iterationRate) == 0) {
            this.resolverUi.indicateProgress();
        }
    }

    /**
     * Writes debugging output.
     *
     * @param depth Current depth of the resolution process
     * @param format Output string passed to {@link String#format(String, Object...)}
     * @param args Arguments for the output string passed to {@link String#format(String, Object...)}
     */
    private void debug(final int depth, final String format, final Object... args) {
        this.resolverUi.debug(depth, format, args);
    }

    /**
     * Attempts to activate the current possibility.
     */
    private void attemptToActivate() {
        debug(getDepth(), "Attempting to activate %s", getPossibility());
        final Vertex<Payload<R, S>, R> existingVertex = getActivated().vertexNamed(getName());
        assert existingVertex != null;

        if (existingVertex.getPayload().isPresent()) {
            debug(getDepth(), "Found existing spec (%s)", existingVertex.getPayload());
            attemptToFilterExistingSpec(existingVertex);
        } else {
            @Nullable final S latest = getPossibility().getLatestVersion();
            @Nullable final R requirement = getRequirement();
            assert requirement != null;
            getPossibility().getPossibilities().removeIf(possibility -> !requirementSatisfiedBy(requirement,
                                                                                                getActivated(),
                                                                                                possibility));

            if (getPossibility().getLatestVersion() == null) {
                // Ensure there's a possibility for better error messages
                if (latest != null) {
                    getPossibility().getPossibilities().add(latest);
                }
                createConflict(null);
                unwindForConflict();
            } else {
                activateNewSpec();
            }
        }
    }

    /**
     * Attempts to update the existing vertex's possibility set with a filtered version.
     *
     * @param vertex Vertex to update
     */
    private void attemptToFilterExistingSpec(final Vertex<Payload<R, S>, R> vertex) {
        final PossibilitySet<R, S> filteredSet = filteredPossibilitySet(vertex);

        if (!filteredSet.getPossibilities().isEmpty()) {
            getActivated().setPayload(getName(), new Payload<>(filteredSet));
            final Set<R> newRequirements = new LinkedHashSet<>(getRequirements());
            pushStateForRequirements(newRequirements, false);
        } else {
            createConflict(null);
            debug(getDepth(), "Unsatisfied by existing spec (%s)", vertex.getPayload());
            unwindForConflict();
        }
    }

    /**
     * Generates a possibility set consisting of the possibilities on the specified vertex that are also in the
     * current state. In other words, the intersection of the current state possibilities and the vertex possibilities.
     *
     * @param vertex Vertex whose possibilities are to be filtered by the current state's possibilities
     * @return The possibilities on the specified vertex filtered by the possibilities in the current state.
     */
    private PossibilitySet<R, S> filteredPossibilitySet(final Vertex<Payload<R, S>, R> vertex) {
        final PossibilitySet<R, S> vertexPossibilitySet = vertex.getPayload().orElseThrow().getPossibilitySet();

        final Set<S> commonPossibilities = new LinkedHashSet<>(vertexPossibilitySet.getPossibilities());
        commonPossibilities.retainAll(getPossibility().getPossibilities());

        return new PossibilitySet<>(vertexPossibilitySet.getDependencies(), commonPossibilities);
    }

    /**
     * Obtains the locked requirement with the specified name.
     *
     * @param requirementName Name of the requirement to find in the locked dependency graph
     * @return Locked requirement with the specified name, or {@code null} if a requirement with the specified
     *      name could not be found.
     */
    @Nullable
    private R lockedRequirementNamed(final String requirementName) {
        final Vertex<R, R> vertex = this.base.vertexNamed(requirementName);
        return (vertex == null) ? null : vertex.getPayload().orElse(null);
    }

    /**
     * Adds the current possibility to the dependency graph of the current state.
     */
    private void activateNewSpec() {
        getConflicts().remove(getName());
        debug(getDepth(), "Activated %s at %s", getName(), getPossibility());
        getActivated().setPayload(getName(), new Payload<>(getPossibility()));
        requireNestedDependenciesFor(getPossibility());
    }

    /**
     * Requires the dependencies that the recently activated specification has.
     *
     * @param possibilitySet Possibility set that has just been activated
     */
    private void requireNestedDependenciesFor(final PossibilitySet<R, S> possibilitySet) {
        @Nullable final S latestVersion = possibilitySet.getLatestVersion();
        assert latestVersion != null;
        final Set<R> nestedDependencies = dependenciesFor(latestVersion);
        debug(getDepth(), "Requiring nested dependencies (%s)", nestedDependencies.stream()
                                                                                  .map(Object::toString)
                                                                                  .collect(Collectors.joining(", ")));

        for (final R d : nestedDependencies) {
            getActivated().addChildVertex(nameForDependency(d), null,
                                          List.of(nameForSpecification(possibilitySet.getLatestVersion())), d);
            final int parentIndex = this.states.size() - 1;
            final List<Integer> parents = this.parentsOf.computeIfAbsent(d, key -> new ArrayList<>());
            if (parents.isEmpty()) {
                parents.add(parentIndex);
            }
        }

        final Set<R> allRequirements = new LinkedHashSet<>(getRequirements());
        allRequirements.addAll(nestedDependencies);
        pushStateForRequirements(allRequirements, !nestedDependencies.isEmpty());
    }

    /**
     * Pushes a new dependency state that encapsulates both existing and new requirements.
     *
     * @param newRequirements New requirements to push
     * @param requiresSort Indicates whether the requirements need to be sorted
     */
    private void pushStateForRequirements(final Set<R> newRequirements, final boolean requiresSort) {
        pushStateForRequirements(newRequirements, requiresSort, getActivated());
    }

    /**
     * Pushes a new dependency state that encapsulates both existing and new requirements.
     *
     * @param newRequirements New requirements to push
     * @param requiresSort Indicates whether the requirements need to be sorted
     * @param newActivated Dependency graph
     */
    private void pushStateForRequirements(final Set<R> newRequirements, final boolean requiresSort,
                                          final DependencyGraph<Payload<R, S>, R> newActivated) {
        final List<R> sortedRequirements = requiresSort
                                           ? sortDependencies(new ArrayList<>(newRequirements),
                                                              newActivated, new HashMap<>(getConflicts()))
                                           : new ArrayList<>(newRequirements);

        final Function<R, Boolean> isRequirementUnique =
                requirement -> this.states.stream()
                                          .noneMatch(state -> Objects.equals(state.getRequirement(), requirement));

        @Nullable R newRequirement = null;
        while (!sortedRequirements.isEmpty()) {
            newRequirement = sortedRequirements.remove(0);

            if (isRequirementUnique.apply(newRequirement)) {
                break;
            }
        }

        final String newName = newRequirement != null ? nameForDependency(newRequirement) : "";
        final List<PossibilitySet<R, S>> possibilities = possibilitiesForRequirement(newRequirement, newActivated);

        final DependencyState<R, S> newState = new DependencyState<>(newName, sortedRequirements,
                                                                     newActivated, newRequirement,
                                                                     possibilities, getDepth(),
                                                                     new HashMap<>(getConflicts()),
                                                                     new ArrayList<>(getUnusedUnwindOptions()));

        handleMissingOrPushDependencyState(newState);
    }

    /**
     * Pushes a new dependency state. If the specification provider allows missing dependencies, and there are no
     * possibilities for that requirement, then the specified state is not pushed, and the vertex in the activated
     * graph is removed, and we continue resolving the remaining requirements.
     *
     * @param state Dependency stat to push if it is not missing
     */
    private void handleMissingOrPushDependencyState(final DependencyState<R, S> state) {
        if (state.getRequirement() != null && state.getPossibilities().isEmpty()
                && allowMissing(state.getRequirement())) {
            state.getActivated().detachVertexNamed(state.getName());
            pushStateForRequirements(new LinkedHashSet<>(state.getRequirements()), false, state.getActivated());
        } else {
            this.states.add(state);
            state.getActivated().tag(state);
        }
    }

    /**
     * Checks a proposed requirement with any existing locked requirement before generating a possibilities for it.
     *
     * @param requirement Proposed requirement
     * @param activated Current dependency graph
     * @return Possibilities for the specified requirement with locked requirements taken into account.
     */
    private List<PossibilitySet<R, S>> possibilitiesForRequirement(@Nullable final R requirement,
                                                                   final DependencyGraph<Payload<R, S>, R> activated) {
        if (requirement == null) {
            return new ArrayList<>();
        }

        if (lockedRequirementNamed(nameForDependency(requirement)) != null) {
            return lockedRequirementPossibilitySet(requirement, activated);
        }

        return groupPossibilities(searchFor(requirement));
    }

    /**
     * Constructs a list of possibility sets containing only the locked requirement, if any.
     *
     * @param requirement Proposed requirement
     * @param activated Current dependency graph
     * @return Possibility sets containing only the locked requirement
     */
    private List<PossibilitySet<R, S>> lockedRequirementPossibilitySet(final R requirement,
                                                                       final DependencyGraph<Payload<R, S>, R> activated) {
        final List<S> allPossibilities = searchFor(requirement);
        @Nullable final R lockedRequirement = lockedRequirementNamed(nameForDependency(requirement));
        assert lockedRequirement != null;

        // Long-winded way to build a possibilities array with either the locked requirement or nothing in it.
        // Required, because the API for locked_requirement is not guaranteed.
        final List<S> lockedPossibilities =
                allPossibilities.stream()
                                .filter(possibility -> requirementSatisfiedBy(lockedRequirement, activated, possibility))
                                .collect(Collectors.toList());

        return groupPossibilities(lockedPossibilities);
    }

    /**
     * Constructs a list of possibility sets with each element representing a group of dependency versions that
     * all have the same sub-dependency version constraints and are contiguous.
     *
     * @param possibilities Possibility candidates for the list
     * @return Contiguous list of possibility sets, each representing a group of dependency versions with the same
     *      sub-dependency version constraints.
     */
    private List<PossibilitySet<R, S>> groupPossibilities(final List<S> possibilities) {
        @Nullable final List<PossibilitySet<R, S>> possibilitySets = new ArrayList<>();
        PossibilitySet<R, S> currentPossibilitySet = null;

        for (int i = possibilities.size() - 1; i >= 0; i--) {
            final S possibility = possibilities.get(i);
            final Set<R> dependencies = dependenciesFor(possibility);

            if (currentPossibilitySet != null && currentPossibilitySet.getDependencies().equals(dependencies)) {
                currentPossibilitySet.getPossibilities().add(0, possibility);
            } else {
                currentPossibilitySet = new PossibilitySet<>(dependencies, List.of(possibility));
                possibilitySets.add(0, currentPossibilitySet);
            }
        }

        return possibilitySets;
    }
}
