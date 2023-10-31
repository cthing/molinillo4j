package org.cthing.molinillo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.cthing.molinillo.graph.Vertex;


/**
 * Base class for a specification provider.
 *
 * @param <R> Type for a requirement
 * @param <S> Type for a specification
 */
public class AbstractSpecificationProvider<R, S> implements SpecificationProvider<R, S> {

    @Override
    public List<S> searchFor(final R dependency) {
        return List.of();
    }

    @Override
    public Set<R> dependenciesFor(final S specification) {
        return Set.of();
    }

    @Override
    public boolean requirementSatisfiedBy(final R requirement, final DependencyGraph<Payload<R, S>, R> activated,
                                          final S specification) {
        return true;
    }

    @Override
    public String nameForDependency(final R dependency) {
        return dependency.toString();
    }

    @Override
    public String nameForSpecification(final S specification) {
        return specification.toString();
    }

    @Override
    public String nameForExplicitDependencySource() {
        return "user-specified dependency";
    }

    @Override
    public String nameForLockingDependencySource() {
        return "Lockfile";
    }

    @Override
    public List<R> sortDependencies(final List<R> dependencies, final DependencyGraph<Payload<R, S>, R> activated,
                                    final Map<String, Conflict<R, S>> conflicts) {
        return dependencies.stream()
                           .sorted(Comparator.comparing(dependency -> {
                               final String name = nameForDependency(dependency);
                               final Optional<Vertex<Payload<R, S>, R>> vertexOptional =
                                       Optional.ofNullable(activated.vertexNamed(name));
                               final int activatedCompare =
                                       vertexOptional.map(v -> (v.getPayload().isEmpty()) ? 1 : 0).orElse(0);
                               final int conflictsCompare =
                                       Optional.ofNullable(conflicts.get(name)).map(c -> 0).orElse(1);
                               return activatedCompare - conflictsCompare;
                           }))
                           .collect(Collectors.toList());
    }

    @Override
    public boolean allowMissing(final R dependency) {
        return false;
    }
}
