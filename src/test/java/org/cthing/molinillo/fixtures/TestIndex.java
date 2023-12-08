package org.cthing.molinillo.fixtures;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
public class TestIndex extends AbstractSpecificationProvider<TestRequirement, TestSpecification> {

    private static final Map<String, Map<String, TestSpecification[]>> SPECS_FROM_FIXTURE = new HashMap<>();

    private final Map<String, TestSpecification[]> specs;
    private final Map<TestRequirement, List<TestSpecification>> searchResults = new HashMap<>();
    private Set<TestRequirement> allowMissingRequirements = new HashSet<>();

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
    public boolean requirementSatisfiedBy(final TestRequirement requirement,
                                          final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                  TestRequirement> activated,
                                          final TestSpecification specification) {
        if (specification.getVersion().isPreRelease() && !requirement.isPreRelease()) {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(specification.getName());
            assert vertex != null;
            if (vertex.requirements().stream().noneMatch(TestRequirement::isPreRelease)) {
                return false;
            }
        }

        if (requirement.isSpecification()) {
            return requirement.getSpecification().getVersion().equals(specification.getVersion());
        }
        return requirement.getDependency().getVersionConstraint().allows(specification.getVersion());
    }

    @Override
    public List<TestSpecification> searchFor(final TestRequirement dependency) {
        return this.searchResults.computeIfAbsent(dependency, dep -> {
            final TestDependency testDependency = dep.getDependency();
            final TestSpecification[] testSpecs = this.specs.computeIfAbsent(testDependency.getName(),
                                                                             key -> new TestSpecification[0]);
            return Arrays.stream(testSpecs)
                         .filter(spec -> testDependency.getVersionConstraint().allows(spec.getVersion()))
                         .collect(Collectors.toList());
        });
    }

    @Override
    public String nameForDependency(final TestRequirement dependency) {
        return dependency.getName();
    }

    @Override
    public String nameForSpecification(final TestSpecification specification) {
        return specification.getName();
    }

    @Override
    public Set<TestRequirement> dependenciesFor(final TestSpecification specification) {
        return specification.getDependencies()
                            .stream()
                            .map(TestRequirement::new)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
        final Function<TestRequirement, Integer> payloadFunction = dep -> {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertex == null || vertex.getPayload().isEmpty()) ? 1 : 0;
        };
        final Function<TestRequirement, Integer> preReleaseFunction = dep -> dep.isPreRelease() ? 0 : 1;
        final Function<TestRequirement, Integer> conflictsFunction =
                dep -> conflicts.containsKey(nameForDependency(dep)) ? 0 : 1;
        final Function<TestRequirement, Integer> countFunction = dep -> {
            final Vertex<Payload<TestRequirement, TestSpecification>, TestRequirement> vertex =
                    activated.vertexNamed(nameForDependency(dep));
            return (vertex == null || vertex.getPayload().isEmpty()) ? searchFor(dep).size() : 0;
        };
        final Comparator<TestRequirement> requirementComparator = Comparator.comparing(payloadFunction)
                                                                           .thenComparing(preReleaseFunction)
                                                                           .thenComparing(conflictsFunction)
                                                                           .thenComparing(countFunction);
        return dependencies.stream()
                           .sorted(requirementComparator)
                           .collect(Collectors.toList());
    }

    @Override
    public boolean allowMissing(final TestRequirement dependency) {
        return this.allowMissingRequirements.contains(dependency);
    }

    public void setAllowMissing(final TestRequirement... requirements) {
        this.allowMissingRequirements = Set.of(requirements);
    }
}
