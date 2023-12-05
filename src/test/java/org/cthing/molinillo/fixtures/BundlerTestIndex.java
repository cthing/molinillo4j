package org.cthing.molinillo.fixtures;

import java.util.Comparator;
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

    public BundlerTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {



        final Function<TestRequirement, Long> payloadFunction = dep -> {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertex == null || vertex.getPayload().isEmpty()) ? 1L : 0L;
        };
        final Function<TestRequirement, Long> rootFunction = dep -> {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertex != null && vertex.isRoot()) ? 0L : 1L;
        };
        final Function<TestRequirement, Long> constainedFunction = this::amountConstrained;
        final Function<TestRequirement, Long> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0L : 1L;
        final Function<TestRequirement, Long> countFunction = dep -> {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertex == null || vertex.getPayload().isEmpty()) ? searchFor(dep).size() : 0L;
        };
        final Comparator<TestRequirement> requirementComparator = Comparator.comparing(payloadFunction)
                                                                            .thenComparing(rootFunction)
                                                                            .thenComparing(constainedFunction)
                                                                            .thenComparing(conflictsFunction)
                                                                            .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    private long amountConstrained(final TestRequirement dependency) {
        final long all = getSpecs().size();
        if (all <= 1) {
            return all - ALL_LEQ_ONE_PENALTY;
        }

        final List<TestSpecification> specs = searchFor(dependency);
        final long num = dependency.isPreRelease()
                         ? specs.size()
                         : specs.stream().filter(spec -> !spec.getVersion().isPreRelease()).count();
        return num - all;
    }
}