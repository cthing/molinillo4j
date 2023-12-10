package org.cthing.molinillo.fixtures;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.cthing.molinillo.ConsoleUI;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Resolver;
import org.cthing.molinillo.graph.Vertex;
import org.cthing.versionparser.Version;
import org.cthing.versionparser.VersionParsingException;
import org.cthing.versionparser.gem.GemVersionScheme;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public final class TestCase {

    private final File fixture;
    private final JsonNode rootNode;
    private final String name;
    private final TestIndex index;
    private final Set<TestDependency> requested;
    private final Set<String> conflicts;

    private TestCase(final File fixture, final ObjectMapper mapper, final JsonNode rootNode) {
        this.fixture = fixture;
        this.rootNode = rootNode;
        this.name = rootNode.get("name").asText();

        final String indexName = rootNode.has("index") ? rootNode.get("index").asText() : "awesome";
        this.index = TestIndex.fromFixture(indexName);

        this.requested = new LinkedHashSet<>();
        rootNode.get("requested").fields().forEachRemaining(entry -> {
            final String requestedName = entry.getKey().replaceAll("\01", "");
            final String[] requestedConstraints = entry.getValue().asText().split("\\s*,\\s*");
            final TestDependency dependency = new TestDependency(requestedName, requestedConstraints);
            this.requested.add(dependency);
        });

        this.conflicts = mapper.convertValue(rootNode.get("conflicts"), new TypeReference<LinkedHashSet<String>>() { });
    }

    public static TestCase fromFixture(final String fixtureName) {
        final File indexFile = new File(TestLocations.CASE_DIR, fixtureName + ".json");
        return fromFixture(indexFile);
    }

    public static TestCase fromFixture(final File fixtureFile) {
        final ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS)
                                                      .enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        try {
            final JsonNode rootNode = mapper.readTree(fixtureFile);
            return new TestCase(fixtureFile, mapper, rootNode);
        } catch (final IOException ex) {
            throw new IllegalStateException("Error parsing test case: " + fixtureFile, ex);
        }
    }

    public static List<TestCase> all() {
        final File[] files = TestLocations.CASE_DIR.listFiles((d, name) -> name.endsWith(".json"));
        assert files != null;
        return Arrays.stream(files).map(TestCase::fromFixture).collect(Collectors.toList());
    }

    public File getFixture() {
        return this.fixture;
    }

    public String getName() {
        return this.name;
    }

    public TestIndex getIndex() {
        return this.index;
    }

    public Set<TestDependency> getRequested() {
        return Collections.unmodifiableSet(this.requested);
    }

    public Set<String> getConflicts() {
        return this.conflicts;
    }

    public DependencyGraph<TestSpecification, TestDependency> getResult() {
        final DependencyGraph<TestSpecification, TestDependency> graph = new DependencyGraph<>();
        final JsonNode resolved = this.rootNode.get("resolved");
        resolved.elements().forEachRemaining(element -> addDependenciesToResGraph(graph, null, element));
        return graph;
    }

    public DependencyGraph<TestDependency, TestDependency> getBase() {
        final DependencyGraph<TestDependency, TestDependency> graph = new DependencyGraph<>();
        final JsonNode base = this.rootNode.get("base");
        base.elements().forEachRemaining(element -> addDependenciesToBaseGraph(graph, null, element,
                                                                               new LinkedHashSet<>()));
        return graph;
    }

    public DependencyGraph<TestSpecification, TestDependency> resolve(final Class<? extends TestIndex> indexClass) {
        final TestIndex testIndex;
        try {
            testIndex = indexClass.getDeclaredConstructor(Map.class).newInstance(this.index.getSpecs());
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }

        final ConsoleUI consoleUi = new ConsoleUI();
        //consoleUi.setDebugMode(true);
        final Resolver<TestDependency, TestSpecification> resolver = new Resolver<>(testIndex, consoleUi);
        return resolver.resolve(getRequested(), getBase());
    }

    private void addDependenciesToBaseGraph(final DependencyGraph<TestDependency, TestDependency> graph,
                                            @Nullable final Vertex<TestDependency, TestDependency> parent,
                                            final JsonNode json) {
        addDependenciesToBaseGraph(graph, parent, json, new LinkedHashSet<>());
    }

    private void addDependenciesToBaseGraph(final DependencyGraph<TestDependency, TestDependency> graph,
                                            @Nullable final Vertex<TestDependency, TestDependency> parent,
                                            final JsonNode json,
                                            final Set<Vertex<TestDependency, TestDependency>> allParents) {
        final String depName = json.get("name").asText();
        final String depVersion = json.get("version").asText();
        final TestDependency testDependency = new TestDependency(depName, depVersion);
        final Vertex<TestDependency, TestDependency> vertex;
        if (parent != null) {
            vertex = graph.addVertex(depName, testDependency, false);
            graph.addEdge(parent, vertex, testDependency);
        } else {
            vertex = graph.addVertex(depName, testDependency, true);
        }

        if (!allParents.add(vertex)) {
            return;
        }

        final JsonNode deps = json.get("dependencies");
        deps.elements().forEachRemaining(dep -> addDependenciesToBaseGraph(graph, vertex, dep, allParents));
    }

    private void addDependenciesToResGraph(final DependencyGraph<TestSpecification, TestDependency> graph,
                                           @Nullable final Vertex<TestSpecification, TestDependency> parent,
                                           final JsonNode json) {
        addDependenciesToResGraph(graph, parent, json, new LinkedHashSet<>());
    }

    private void addDependenciesToResGraph(final DependencyGraph<TestSpecification, TestDependency> graph,
                                           @Nullable final Vertex<TestSpecification, TestDependency> parent,
                                           final JsonNode json,
                                           final Set<Vertex<TestSpecification, TestDependency>> allParents) {
        final String specName = json.get("name").asText();
        final String specVersionStr = json.get("version").asText();
        final Version specVersion;
        try {
            specVersion = GemVersionScheme.parseVersion(specVersionStr);
        } catch (final VersionParsingException ex) {
            throw new IllegalStateException(ex);
        }
        final Optional<TestSpecification> specOpt = Arrays.stream(this.index.getSpecs().get(specName))
                                                          .filter(testSpec -> testSpec.getVersion().compareTo(specVersion) == 0)
                                                          .findFirst();
        final TestSpecification spec = specOpt.orElseThrow();
        final Vertex<TestSpecification, TestDependency> vertex;
        if (parent != null) {
            vertex = graph.addVertex(specName, spec, false);
            graph.addEdge(parent, vertex, new TestDependency(spec.getName(), specVersionStr));
        } else {
            vertex = graph.addVertex(specName, spec, true);
        }

        if (!allParents.add(vertex)) {
            return;
        }

        final JsonNode deps = json.get("dependencies");
        deps.elements().forEachRemaining(dep -> addDependenciesToResGraph(graph, vertex, dep, allParents));
    }
}
