package org.cthing.molinillo.fixtures;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.graph.Vertex;


public class BerkshelfTestIndex extends TestIndex {

    public BerkshelfTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
        final Function<TestRequirement, Integer> payloadFunction = dep -> {
            final Optional<Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<TestRequirement, Integer> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Function<TestRequirement, Integer> versionsFunction = dep -> {
            final Optional<Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? versionsOf(nameForDependency(dep)) : 0;
        };
        final Comparator<TestRequirement> requirementComparator = Comparator.comparing(payloadFunction)
                                                                            .thenComparing(conflictsFunction)
                                                                            .thenComparing(versionsFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    private int versionsOf(final String dependencyName) {
        return getSpecs().computeIfAbsent(dependencyName, k -> new TestSpecification[0]).length;
    }
}
