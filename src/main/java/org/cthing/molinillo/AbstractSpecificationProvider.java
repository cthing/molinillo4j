package org.cthing.molinillo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.graph.Vertex;


/**
 * A convenience base class for a specification provider.
 *
 * @param <R> Type for a requirement
 * @param <S> Type for a specification
 */
public abstract class AbstractSpecificationProvider<R, S> implements SpecificationProvider<R, S> {

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
        final Function<R, Integer> payloadFunction = dep -> {
            final Vertex<Payload<R, S>, R> vertex = activated.vertexNamed(nameForDependency(dep)).orElse(null);
            return (vertex == null || vertex.getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<R, Integer> conflictsFunction = dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Comparator<R> requirementComparator = Comparator.comparing(payloadFunction)
                                                              .thenComparing(conflictsFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    @Override
    public boolean allowMissing(final R dependency) {
        return false;
    }
}
