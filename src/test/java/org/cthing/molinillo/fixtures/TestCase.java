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
import java.util.function.Function;
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
    private final Set<TestRequirement> requested;
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
            this.requested.add(new TestRequirement(dependency));
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

    public Set<TestRequirement> getRequested() {
        return Collections.unmodifiableSet(this.requested);
    }

    public Set<String> getConflicts() {
        return this.conflicts;
    }

    public DependencyGraph<TestSpecification, TestRequirement> getResult() {
        final DependencyGraph<TestSpecification, TestRequirement> graph = new DependencyGraph<>();
        final JsonNode resolved = this.rootNode.get("resolved");
        resolved.elements().forEachRemaining(element -> addDependenciesToGraph(graph, null, element,
                                                                               dependency -> dependency));
        return graph;
    }

    public DependencyGraph<TestRequirement, TestRequirement> getBase() {
        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final JsonNode base = this.rootNode.get("base");
        base.elements().forEachRemaining(element -> addDependenciesToGraph(graph, null, element, TestRequirement::new));
        return graph;
    }

    public DependencyGraph<TestSpecification, TestRequirement> resolve(final Class<? extends TestIndex> indexClass) {
        final TestIndex testIndex;
        try {
            testIndex = indexClass.getDeclaredConstructor(Map.class).newInstance(this.index.getSpecs());
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }

        final ConsoleUI consoleUi = new ConsoleUI();
        //consoleUi.setDebugMode(true);
        final Resolver<TestRequirement, TestSpecification> resolver = new Resolver<>(testIndex, consoleUi);
        return resolver.resolve(getRequested(), getBase());
    }

    private <R> void addDependenciesToGraph(final DependencyGraph<R, TestRequirement> graph,
                                            @Nullable final Vertex<R, TestRequirement> parent,
                                            final JsonNode json, final Function<TestSpecification, R> payloadFunc) {
        addDependenciesToGraph(graph, parent, json, payloadFunc, new LinkedHashSet<>());
    }

    private <R> void addDependenciesToGraph(final DependencyGraph<R, TestRequirement> graph,
                                            @Nullable final Vertex<R, TestRequirement> parent,
                                            final JsonNode json, final Function<TestSpecification, R> payloadFunc,
                                            final Set<Vertex<R, TestRequirement>> allParents) {
        final String specName = json.get("name").asText();
        final Version specVersion;
        try {
            specVersion = GemVersionScheme.parseVersion(json.get("version").asText());
        } catch (final VersionParsingException ex) {
            throw new IllegalStateException(ex);
        }
        final Optional<TestSpecification> dependencyOpt = Arrays.stream(this.index.getSpecs().get(specName))
                                                                .filter(testSpec -> testSpec.getVersion().compareTo(specVersion) == 0)
                                                                .findFirst();
        final TestSpecification dependency = dependencyOpt.orElseThrow();
        final Vertex<R, TestRequirement> vertex;
        if (parent != null) {
            vertex = graph.addVertex(specName, payloadFunc.apply(dependency), false);
            graph.addEdge(parent, vertex, new TestRequirement(dependency));
        } else {
            vertex = graph.addVertex(specName, payloadFunc.apply(dependency), true);
        }

        if (!allParents.add(vertex)) {
            return;
        }

        final JsonNode deps = json.get("dependencies");
        deps.elements().forEachRemaining(dep -> addDependenciesToGraph(graph, vertex, dep, payloadFunc, allParents));
    }
}
