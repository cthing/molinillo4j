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
    public boolean requirementSatisfiedBy(final TestRequirement requirement,
                                          final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> activated,
                                          final TestSpecification specification) {
        final TestDependency dependency = requirement.isSpecification()
                                          ? new TestDependency(requirement.getName(),
                                                               requirement.getSpecification().getVersion().toString())
                                          : requirement.getDependency();
        final Version version = specification.getVersion();
        if (!dependency.getVersionConstraint().allows(version)) {
            return false;
        }

        final PossibleVersions possibleVersions = possibilityVersionsForRootName(dependency, activated);
        if (!possibleVersions.existingVersions.isEmpty() && !possibleVersions.existingVersions.contains(version)) {
            return false;
        }
        return !version.isPreRelease() || possibleVersions.prereleaseRequirement;
    }

    @Override
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
        final Function<TestRequirement, Integer> payloadFunction = dep -> {
            final Optional<Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<TestRequirement, Integer> preReleaseFunction = dep -> dep.isPreRelease() ? 0 : 1;
        final Function<TestRequirement, Integer> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Function<TestRequirement, Integer> countFunction = dep -> searchFor(dep).size();
        final Comparator<TestRequirement> requirementComparator = Comparator.comparing(payloadFunction)
                                                                            .thenComparing(preReleaseFunction)
                                                                            .thenComparing(conflictsFunction)
                                                                            .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    private PossibleVersions possibilityVersionsForRootName(final TestDependency dependency,
                                                            final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                                TestRequirement> activated) {
        boolean prereleaseRequirement = dependency.isPreRelease();
        List<Version> existingVersions = new ArrayList<>();
        for (final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex : activated.getVertices().values()) {
            final Optional<Payload<TestRequirement, TestSpecification>> payloadOpt = vertex.getPayload();
            if (payloadOpt.isEmpty()) {
                continue;
            }
            final Payload<TestRequirement, TestSpecification> payload = payloadOpt.get();

            final String vertexFirstName = vertex.getName().split("/")[0];
            final String dependencyFirstName = dependency.getName().split("/")[0];
            if (!vertexFirstName.equals(dependencyFirstName)) {
                continue;
            }

            prereleaseRequirement |= vertex.requirements().stream().anyMatch(TestRequirement::isPreRelease);

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
