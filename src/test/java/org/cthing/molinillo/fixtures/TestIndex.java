package org.cthing.molinillo.fixtures;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.AbstractSpecificationProvider;
import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.graph.Vertex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@SuppressWarnings("MethodDoesntCallSuperMethod")
public class TestIndex extends AbstractSpecificationProvider<TestDependency, TestSpecification> {

    private static final Map<String, Map<String, TestSpecification[]>> SPECS_FROM_FIXTURE = new HashMap<>();

    private final Map<String, TestSpecification[]> specs;
    private final Map<TestDependency, List<TestSpecification>> searchResults = new HashMap<>();
    private Set<TestDependency> allowMissingRequirements = new HashSet<>();

    public TestIndex(final Map<String, TestSpecification[]> specsByName) {
        this.specs = specsByName;
    }

    public static TestIndex fromFixture(final String fixtureName) {
        final Map<String, TestSpecification[]> specsByName = SPECS_FROM_FIXTURE.computeIfAbsent(fixtureName,
                                                                                                TestIndex::loadFixture);
        return new TestIndex(specsByName);
    }

    @SuppressWarnings("Convert2Diamond")
    private static Map<String, TestSpecification[]> loadFixture(final String fixtureName) {
        final File indexFile = new File(TestLocations.INDEX_DIR, fixtureName + ".json");
        final ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS)
                                                      .enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        try {
            final Map<String, TestSpecification[]> specMap =
                    mapper.readValue(indexFile, new TypeReference<Map<String, TestSpecification[]>>() { });
            return specMap.entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey,
                                                    entry -> Arrays.stream(entry.getValue())
                                                                   .sorted(Comparator.comparing(TestSpecification::getVersion))
                                                                   .toArray(TestSpecification[]::new)));
        } catch (final IOException ex) {
            throw new IllegalStateException("Error parsing index: " + fixtureName, ex);
        }
    }

    public Map<String, TestSpecification[]> getSpecs() {
        return this.specs;
    }

    @Override
    public boolean requirementSatisfiedBy(final TestDependency requirement,
                                          final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                  TestDependency> activated,
                                          final TestSpecification specification) {
        if (specification.getVersion().isPreRelease() && !requirement.isPreRelease()) {
            final Vertex<Payload<TestDependency, TestSpecification>, TestDependency> vertex =
                    activated.vertexNamed(specification.getName()).orElseThrow();
            if (vertex.requirements().stream().noneMatch(TestDependency::isPreRelease)) {
                return false;
            }
        }

        return requirement.getVersionConstraint().allows(specification.getVersion());
    }

    @Override
    public List<TestSpecification> searchFor(final TestDependency dependency) {
        return this.searchResults.computeIfAbsent(dependency, dep -> {
            final TestSpecification[] testSpecs = this.specs.computeIfAbsent(dep.getName(),
                                                                             key -> new TestSpecification[0]);
            return Arrays.stream(testSpecs)
                         .filter(spec -> dep.getVersionConstraint().allows(spec.getVersion()))
                         .collect(Collectors.toList());
        });
    }

    @Override
    public String nameForDependency(final TestDependency dependency) {
        return dependency.getName();
    }

    @Override
    public String nameForSpecification(final TestSpecification specification) {
        return specification.getName();
    }

    @Override
    public Set<TestDependency> dependenciesFor(final TestSpecification specification) {
        return specification.getDependencies();
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
        final Function<TestDependency, Integer> countFunction = dep -> {
            final Optional<Vertex<Payload<TestDependency, TestSpecification>, TestDependency>> vertexOpt =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertexOpt.isEmpty() || vertexOpt.get().getPayload().isEmpty()) ? searchFor(dep).size() : 0;
        };
        final Comparator<TestDependency> requirementComparator = Comparator.comparing(payloadFunction)
                                                                           .thenComparing(preReleaseFunction)
                                                                           .thenComparing(conflictsFunction)
                                                                           .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    @Override
    public boolean allowMissing(final TestDependency dependency) {
        return this.allowMissingRequirements.contains(dependency);
    }

    public void setAllowMissing(final TestDependency... requirements) {
        this.allowMissingRequirements = Set.of(requirements);
    }
}
