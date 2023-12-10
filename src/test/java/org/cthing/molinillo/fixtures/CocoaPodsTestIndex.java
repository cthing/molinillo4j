package org.cthing.molinillo.fixtures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.graph.Vertex;
import org.cthing.versionparser.Version;


@SuppressWarnings("MethodDoesntCallSuperMethod")
public class CocoaPodsTestIndex extends TestIndex {

    private record PossibleVersions(List<Version> existingVersions, boolean prereleaseRequirement) {
    }

    public CocoaPodsTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    public boolean requirementSatisfiedBy(final TestDependency requirement,
                                          final DependencyGraph<Payload<TestDependency, TestSpecification>, TestDependency> activated,
                                          final TestSpecification specification) {
        final Version version = specification.getVersion();
        if (!requirement.getVersionConstraint().allows(version)) {
            return false;
        }

        final PossibleVersions possibleVersions = possibilityVersionsForRootName(requirement, activated);
        if (!possibleVersions.existingVersions.isEmpty() && !possibleVersions.existingVersions.contains(version)) {
            return false;
        }
        return !version.isPreRelease() || possibleVersions.prereleaseRequirement;
    }

    @Override
    public List<TestDependency> sortDependencies(final List<TestDependency> dependencies,
                                                 final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                         TestDependency> activated,
                                                 final Map<String, Conflict<TestDependency, TestSpecification>> conflicts) {
        final Function<TestDependency, Integer> payloadFunction = dep -> {
            final Optional<Vertex<Payload<TestDependency, TestSpecification>, TestDependency>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<TestDependency, Integer> preReleaseFunction = dep -> dep.isPreRelease() ? 0 : 1;
        final Function<TestDependency, Integer> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Function<TestDependency, Integer> countFunction = dep -> searchFor(dep).size();
        final Comparator<TestDependency> requirementComparator = Comparator.comparing(payloadFunction)
                                                                           .thenComparing(preReleaseFunction)
                                                                           .thenComparing(conflictsFunction)
                                                                           .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    private PossibleVersions possibilityVersionsForRootName(final TestDependency dependency,
                                                            final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                                    TestDependency> activated) {
        boolean prereleaseRequirement = dependency.isPreRelease();
        List<Version> existingVersions = new ArrayList<>();
        for (final Vertex<Payload<TestDependency, TestSpecification>, TestDependency> vertex : activated.getVertices().values()) {
            final Optional<Payload<TestDependency, TestSpecification>> payloadOpt = vertex.getPayload();
            if (payloadOpt.isEmpty()) {
                continue;
            }
            final Payload<TestDependency, TestSpecification> payload = payloadOpt.get();

            final String vertexFirstName = vertex.getName().split("/")[0];
            final String dependencyFirstName = dependency.getName().split("/")[0];
            if (!vertexFirstName.equals(dependencyFirstName)) {
                continue;
            }

            prereleaseRequirement |= vertex.requirements().stream().anyMatch(TestDependency::isPreRelease);

            if (payload.isPossibilitySet()) {
                existingVersions.addAll(payload.getPossibilitySet()
                                               .getPossibilities()
                                               .stream()
                                               .map(TestSpecification::getVersion)
                                               .toList());
            } else {
                existingVersions.add(payload.getSpecification().getVersion());
            }
        }

        existingVersions = existingVersions.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return new PossibleVersions(existingVersions, prereleaseRequirement);
    }
}
