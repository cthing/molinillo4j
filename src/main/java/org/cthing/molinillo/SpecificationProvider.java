package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides information about package metadata and dependencies to the resolver. This allows the resolver class to
 * remain generic to the type of package being resolved. This interface acts as an adapter between the domain specific
 * model and the resolution algorithm.
 *
 * @param <R> Type for a requirement
 * @param <S> Type for a specification
 */
public interface SpecificationProvider<R, S> {

    /**
     * Searches for the specifications that match the given dependency. The specifications in the returned list will
     * be considered in reverse order. The means that the latest version should be last. This method should only
     * depend on the specified dependency.
     *
     * @param dependency Dependency to match
     * @return Specifications that satisfy the specified dependency.
     */
    List<S> searchFor(R dependency);

    /**
     * Provides the dependencies from the specified specification. Note that this method should only depend on the
     * specified specification.
     *
     * @param specification Package metadata from which the package dependencies can be obtained
     * @return Dependencies that are required by the specified specification.
     */
    Set<R> dependenciesFor(S specification);

    /**
     * Indicates whether the specified requirement is satisfied by the specified specification in the context of
     * the specified activated dependency graph.
     *
     * @param requirement Requirement to test
     * @param activated Current dependency graph
     * @param specification Package metadata
     * @return {@code true} if the requirement is satisfied by the specification in the context of the current
     *      activated dependency graph.
     */
    boolean requirementSatisfiedBy(R requirement, DependencyGraph<Payload<R, S>, R> activated, S specification);

    /**
     * Provides the name for the specified dependency. Note that this method should only depend on the specified
     * dependency.
     *
     * @param dependency Dependency whose name is to be returned
     * @return Name corresponding to the specified dependency.
     */
    String nameForDependency(R dependency);

    /**
     * Provides the name for the specified specification. Note that this method should only depend on the specified
     * dependency.
     *
     * @param specification Specification whose name is to be returned
     * @return Name corresponding to the specified dependency.
     */
    String nameForSpecification(S specification);

    /**
     * Provides the name of the source of explicit dependencies. These are the dependencies directly passed to the
     * resolver.
     *
     * @return Name of the source of explicit dependencies.
     */
    String nameForExplicitDependencySource();

    /**
     * Provides the name of the source of locked dependencies. These are the dependencies passed to the resolver
     * as locked versions.
     *
     * @return Name of the source of locked dependencies.
     */
    String nameForLockingDependencySource();

    /**
     * Sorts the specified dependencies so that the ones that are easiest to resolve are first. Typically, the
     * easiest to resolve is defined as:
     * <ul>
     *     <li>Is the dependency already activated?</li>
     *     <li>How relaxed are the requirements?</li>
     *     <li>Are there any conflicts for this dependency?</li>
     *     <li>How many possibilities are there to satisfy this dependency?</li>
     * </ul>
     *
     * @param dependencies Dependencies to sort
     * @param activated Current dependency graph
     * @param conflicts Resolution conflicts
     * @return Sorted copy of the specified dependencies.
     */
    List<R> sortDependencies(List<R> dependencies, DependencyGraph<Payload<R, S>, R> activated,
                             Map<String, Conflict<R, S>> conflicts);

    /**
     * Indicates whether the specified dependency, which has no possible matching specifications, can be safely
     * skipped.
     *
     * @param dependency Requirement to test
     * @return {@code true} if the specified dependency can be safely skipped.
     */
    boolean allowMissing(R dependency);
}
