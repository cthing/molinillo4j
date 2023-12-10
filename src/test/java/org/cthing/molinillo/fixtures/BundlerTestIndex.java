package org.cthing.molinillo.fixtures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.graph.Vertex;


/**
 * Certain bugs only occur when Molinillo processes dependencies in a specific order for the given
 * index and demands. The sorting logic in this index ensures the error case is encountered.
 */
public class BundlerTestIndex extends TestIndex {

    private static final int ALL_LEQ_ONE_PENALTY = 1_000_000;

    private final Map<String, Long> amountConstrained = new HashMap<>();

    public BundlerTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<TestDependency> sortDependencies(final List<TestDependency> dependencies,
                                                  final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                          TestDependency> activated,
                                                  final Map<String, Conflict<TestDependency, TestSpecification>> conflicts) {
        final Function<TestDependency, Long> payloadFunction = dep -> {
            final Vertex<Payload<TestDependency, TestSpecification>, TestDependency> vertex =
                    activated.vertexNamed(nameForDependency(dep)).orElseThrow();
            return vertex.getPayload().isPresent() ? 0L : 1L;
        };
        final Function<TestDependency, Long> rootFunction = dep -> {
            final Vertex<Payload<TestDependency, TestSpecification>, TestDependency> vertex =
                    activated.vertexNamed(nameForDependency(dep)).orElseThrow();
            return vertex.isRoot() ? 0L : 1L;
        };
        final Function<TestDependency, Long> constainedFunction = this::amountConstrained;
        final Function<TestDependency, Long> conflictsFunction =
                dep -> (conflicts.get(nameForDependency(dep)) != null) ? 0L : 1L;
        final Function<TestDependency, Long> countFunction = dep -> {
            final Vertex<Payload<TestDependency, TestSpecification>, TestDependency> vertex =
                    activated.vertexNamed(nameForDependency(dep)).orElseThrow();
            return vertex.getPayload().isEmpty() ? searchFor(dep).size() : 0L;
        };
        final Comparator<TestDependency> requirementComparator = Comparator.comparing(payloadFunction)
                                                                           .thenComparing(rootFunction)
                                                                           .thenComparing(constainedFunction)
                                                                           .thenComparing(conflictsFunction)
                                                                           .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    protected long amountConstrained(final TestDependency dependency) {
        return this.amountConstrained.computeIfAbsent(dependency.getName(), key -> {
            final long all = getSpecs().computeIfAbsent(dependency.getName(), k -> new TestSpecification[0]).length;
            if (all <= 1) {
                return all - ALL_LEQ_ONE_PENALTY;
            }

            final List<TestSpecification> specs = searchFor(dependency);
            final long num = dependency.isPreRelease()
                             ? specs.size()
                             : specs.stream().filter(spec -> !spec.getVersion().isPreRelease()).count();
            return num - all;
        });
    }
}
