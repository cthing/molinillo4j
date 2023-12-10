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
    public List<TestDependency> sortDependencies(final List<TestDependency> dependencies,
                                                 final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                         TestDependency> activated,
                                                 final Map<String, Conflict<TestDependency, TestSpecification>> conflicts) {
        final Function<TestDependency, Integer> payloadFunction = dep -> {
            final Optional<Vertex<Payload<TestDependency, TestSpecification>, TestDependency>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<TestDependency, Integer> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Function<TestDependency, Integer> versionsFunction = dep -> {
            final Optional<Vertex<Payload<TestDependency, TestSpecification>, TestDependency>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty())
                   ? versionsOf(nameForDependency(dep))
                   : 0;
        };
        final Comparator<TestDependency> requirementComparator = Comparator.comparing(payloadFunction)
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
