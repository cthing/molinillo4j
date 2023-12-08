package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cthing.molinillo.errors.CircularDependencyError;
import org.cthing.molinillo.errors.ResolverError;
import org.cthing.molinillo.errors.VersionConflictError;
import org.cthing.molinillo.fixtures.BerkshelfTestIndex;
import org.cthing.molinillo.fixtures.BundlerNoPenaltyTestIndex;
import org.cthing.molinillo.fixtures.BundlerTestIndex;
import org.cthing.molinillo.fixtures.CocoaPodsTestIndex;
import org.cthing.molinillo.fixtures.RandomTestIndex;
import org.cthing.molinillo.fixtures.TestCase;
import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestIndex;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class ResolutionTest {

    private static final List<Class<? extends TestIndex>> INDEX_CLASSES = List.of(
            TestIndex.class,
            BundlerTestIndex.class,
            BundlerNoPenaltyTestIndex.class,
            BundlerReverseTestIndex.class,
            CocoaPodsTestIndex.class,
            BerkshelfTestIndex.class,
            RandomTestIndex.class
    );

    private static boolean ignoreTest(final Class<? extends TestIndex> indexClass, final TestCase testCase) {
        // This index occasionally finds orders that are very slow to resolve (e.g. seconds to minutes).
        // This is a problem in the Molinillo algorithm and the Molinillo project team has not found a
        // way to speed it up yet.
        return indexClass.equals(RandomTestIndex.class)
                && "complex_conflict_unwinding.json".equals(testCase.getFixture().getName());
    }

    @TestFactory
    public List<DynamicTest> resolveTestFactory() {
        final List<DynamicTest> tests = new ArrayList<>();
        for (final Class<? extends TestIndex> indexClass : INDEX_CLASSES) {
            for (final TestCase testCase : TestCase.all()) {
                if (ignoreTest(indexClass, testCase)) {
                    continue;
                }

                final String testName = indexClass.getSimpleName() + ": " + testCase.getName()
                        + " (" + testCase.getFixture().getName() + ")";
                final DynamicTest dynamicTest =
                        DynamicTest.dynamicTest(testName, () -> {
                            final Set<String> conflicts = testCase.getConflicts();
                            if (conflicts.isEmpty()) {
                                final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> result = testCase.resolve(indexClass);
                                assertThat(result).isEqualTo(testCase.getResult());
                            } else {
                                final Throwable throwable = catchThrowableOfType(() -> testCase.resolve(indexClass), ResolverError.class);
                                if (throwable instanceof final CircularDependencyError error) {
                                    final List<Payload<TestRequirement, TestSpecification>> payloads = error.getPayloads();
                                    final Set<String> deps = payloads.stream()
                                                                     .flatMap(payload -> payload.getPossibilitySet().getDependencies()
                                                                                                .stream()
                                                                                                .map(TestRequirement::getName))
                                                                     .collect(Collectors.toSet());
                                    assertThat(deps).isEqualTo(conflicts);
                                } else if (throwable instanceof final VersionConflictError error) {
                                    assertThat(error.getConflicts().keySet()).isEqualTo(conflicts);
                                } else {
                                    fail("Unexpected exception thrown: " + throwable);
                                }
                            }
                        });
                tests.add(dynamicTest);
            }
        }
        return tests;
    }

    @Test
    @DisplayName("Includes the source of a user-specified unsatisfied dependency")
    public void testConflictSource() {
        final TestIndex testIndex = TestIndex.fromFixture("awesome");
        final TestRequirement dep = new TestRequirement(new TestDependency("missing", "3.0"));
        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(testIndex, new ConsoleUI(),
                                                                                         Set.of(dep), graph);
        final VersionConflictError versionConflictError = catchThrowableOfType(resolver::resolve,
                                                                               VersionConflictError.class);
        assertThat(versionConflictError.getMessage()).isEqualTo("""
                             Unable to satisfy the following requirements:

                             - 'missing ([3.0])' required by 'user-specified dependency'""");
        assertThat(versionConflictError.messageWithTrees()).isEqualTo("""
                            Resolution could not find compatible versions for possibility named 'missing'
                              In user-specified dependency:
                                missing ([3.0])""");
    }

    @Test
    @DisplayName("Throws conflicts with requirement trees")
    public void testConflictsWithTrees() {
        final TestCase testCase =
                TestCase.all()
                        .stream()
                        .filter(tc -> "yields conflicts if a child dependency is not resolved".equals(tc.getName()))
                        .findFirst()
                        .orElseThrow();
        final VersionConflictError versionConflictError = catchThrowableOfType(() -> testCase.resolve(TestIndex.class),
                                                                               VersionConflictError.class);
        assertThat(versionConflictError.getMessage()).isEqualTo("""
                             Unable to satisfy the following requirements:

                             - 'json ([1.7.7,))' required by 'berkshelf (2.0.7)'
                             - 'json ([1.4.4,1.7.7])' required by 'chef (10.26)'""");
        assertThat(versionConflictError.messageWithTrees()).isEqualTo("""
            Resolution could not find compatible versions for possibility named 'json'
              In user-specified dependency:
                chef_app_error ([0,)) was resolved to chef_app_error (1.0.0), which depends on
                  chef ([10.26,11.ZZZ)) was resolved to chef (10.26), which depends on
                    json ([1.4.4,1.7.7])

                chef_app_error ([0,)) was resolved to chef_app_error (1.0.0), which depends on
                  berkshelf ([2.0,3.ZZZ)) was resolved to berkshelf (2.0.7), which depends on
                    json ([1.7.7,))""");
    }

    @Test
    @DisplayName("Succeeds when allowMissing returns true for the only requirement")
    public void testAllowMissingOnlyRequirement() {
        final TestIndex testIndex = TestIndex.fromFixture("awesome");
        final TestRequirement dep = new TestRequirement(new TestDependency("missing", "3.0"));
        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(testIndex, new ConsoleUI(),
                                                                                         Set.of(dep), graph);

        testIndex.setAllowMissing(dep);
        assertThat(resolver.resolve().getVertices()).isEmpty();
    }

    @Test
    @DisplayName("Succeeds when allowMissing returns true for a nested requirement")
    public void testAllowMissingNestedRequirement() {
        final TestRequirement dep1 = new TestRequirement(new TestDependency("actionpack", "1.2.3"));
        final TestRequirement dep2 = new TestRequirement(new TestDependency("activesupport", "1.2.3"));

        final TestIndex testIndex = TestIndex.fromFixture("awesome");
        final TestIndex spyIndex = spy(testIndex);
        when(spyIndex.searchFor(dep2)).thenReturn(List.of());

        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(spyIndex, new ConsoleUI(),
                                                                                         Set.of(dep1), graph);

        spyIndex.setAllowMissing(dep2);
        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> results = resolver.resolve();
        final List<TestSpecification> testSpecs = results.getVertices()
                                                         .values()
                                                         .stream()
                                                         .map(vertex -> vertex.getPayload()
                                                                              .orElseThrow()
                                                                              .getSpecification())
                                                         .collect(Collectors.toList());
        assertThat(testSpecs).containsExactly(new TestSpecification("actionpack", "1.2.3",
                                                                    Map.of("activesupport", "1.2.3")));
    }

    @Test
    @DisplayName("Only cleans up orphaned vertices after swapping")
    public void testOrphanCleanup() {
        class Index extends TestIndex {

            Index(final Map<String, TestSpecification[]> specsByName) {
                super(specsByName);
            }

            @Override
            @SuppressWarnings("MethodDoesntCallSuperMethod")
            public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                          final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> activated,
                                                          final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
                final TestRequirement req1 = new TestRequirement(new TestDependency("c", ">= 1.0.0"));
                final TestRequirement req2 = new TestRequirement(new TestDependency("b", "< 2.0.0"));
                final TestRequirement req3 = new TestRequirement(new TestDependency("a", "< 2.0.0"));
                final TestRequirement req4 = new TestRequirement(new TestDependency("c", "= 1.0.0"));
                final List<TestRequirement> reqs = List.of(req1, req2, req3, req4);

                return dependencies.stream()
                                   .sorted(Comparator.comparing(dep -> {
                                       final int idx = reqs.indexOf(dep);
                                       return (idx < 0) ? 999 : idx;
                                   }))
                                   .collect(Collectors.toList());

            }
        }

        final Index index = new Index(new HashMap<>(Map.of(
                "a", new TestSpecification[] {
                        new TestSpecification("a", "1.0.0", Map.of("z", "= 2.0.0")),
                        new TestSpecification("a", "2.0.0", Map.of("z", "= 1.0.0")),
                },
                "b", new TestSpecification[] {
                        new TestSpecification("b", "1.0.0", Map.of("a", "< 2")),
                        new TestSpecification("b", "2.0.0", Map.of("a", "< 2")),
                },
                "c", new TestSpecification[] {
                        new TestSpecification("c", "1.0.0", Map.of()),
                        new TestSpecification("c", "2.0.0", Map.of("b", "< 2")),
                },
                "z", new TestSpecification[] {
                        new TestSpecification("z", "1.0.0", Map.of()),
                        new TestSpecification("z", "2.0.0", Map.of()),
                })
        ));

        final TestRequirement dep1 = new TestRequirement(new TestDependency("c", "= 1.0.0"));
        final TestRequirement dep2 = new TestRequirement(new TestDependency("c", ">= 1.0.0"));
        final TestRequirement dep3 = new TestRequirement(new TestDependency("z", ">= 1.0.0"));
        final Set<TestRequirement> deps = Set.of(dep1, dep2, dep3);

        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(index, new ConsoleUI(),
                                                                                         deps, graph);
        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> results = resolver.resolve();
        final List<TestSpecification> testSpecs = results.getVertices()
                                                         .values()
                                                         .stream()
                                                         .map(vertex -> vertex.getPayload()
                                                                              .orElseThrow()
                                                                              .getSpecification())
                                                         .collect(Collectors.toList());
        assertThat(testSpecs).containsExactly(new TestSpecification("c", "1.0.0", Map.of()),
                                              new TestSpecification("z", "2.0.0", Map.of()));
    }

    @Test
    @DisplayName("Does not reset parent tracking after swapping when another requirement led to the child")
    public void testParentTracking() {
        final TestRequirement dep1 = new TestRequirement(new TestDependency("autobuild"));
        final TestRequirement dep2 = new TestRequirement(new TestDependency("pastel"));
        final TestRequirement dep3 = new TestRequirement(new TestDependency("tty-prompt"));
        final TestRequirement dep4 = new TestRequirement(new TestDependency("tty-table"));
        final Set<TestRequirement> deps = Set.of(dep1, dep2, dep3, dep4);

        final TestIndex index = BundlerTestIndex.fromFixture("rubygems-2017-01-24");
        index.getSpecs().put("autobuild", new TestSpecification[] {
                new TestSpecification("autobuild", "0.1.0", Map.of("tty-prompt", ">= 0.6.0, ~> 0.6.0",
                                                                   "pastel", ">= 0.6.0, ~> 0.6.0")),
        });

        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(index, new ConsoleUI(),
                                                                                         deps, graph);

        deps.forEach(index::searchFor);

        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> results = resolver.resolve();
        final List<TestSpecification> testSpecs = results.getVertices()
                                                         .values()
                                                         .stream()
                                                         .map(vertex -> vertex.getPayload()
                                                                              .orElseThrow()
                                                                              .getSpecification())
                                                         .collect(Collectors.toList());
        assertThat(testSpecs).contains(
                new TestSpecification("pastel", "0.6.1",
                                      Map.of("equatable", "~> 0.5.0", "tty-color", "~> 0.3.0")),
                new TestSpecification("tty-table", "0.6.0",
                                      Map.of("equatable", "~> 0.5.0",
                                             "necromancer", "~> 0.3.0",
                                             "pastel", "~> 0.6.0",
                                             "tty-screen", "~> 0.5.0",
                                             "unicode-display_width", "~> 1.1.0",
                                             "verse", "~> 0.5.0"))
        );
    }

    @Test
    @DisplayName("Includes the whole path in circular dependency errors")
    public void testCircularDependencyPath() {
        final TestIndex testIndex = new TestIndex(new HashMap<>(Map.of(
                "a", new TestSpecification[] {
                        new TestSpecification("a", "1.0.0", Map.of("b", "= 1.0.0")),
                },
                "b", new TestSpecification[] {
                        new TestSpecification("b", "1.0.0", Map.of("c", "= 1.0.0")),
                },
                "c", new TestSpecification[] {
                        new TestSpecification("c", "1.0.0", Map.of("d", "= 1.0.0")),
                },
                "d", new TestSpecification[] {
                        new TestSpecification("d", "1.0.0", Map.of("a", "= 1.0.0")),
                })
        ));

        final Set<TestRequirement> deps = Set.of(new TestRequirement(new TestDependency("a")));

        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
        final Resolution<TestRequirement, TestSpecification> resolver = new Resolution<>(testIndex, new ConsoleUI(),
                                                                                         deps, graph);
        assertThatExceptionOfType(CircularDependencyError.class)
                .isThrownBy(resolver::resolve)
                .withMessage("There is a circular dependency between a and b and c and d");
    }

    @TestFactory
    public List<DynamicTest> equallyValidTestFactory() {
        final List<DynamicTest> tests = new ArrayList<>();
        for (final Class<? extends TestIndex> indexClass : INDEX_CLASSES) {
            final String testName = indexClass.getSimpleName() + ": Can resolve when two resolutions are equally valid";
            final DynamicTest dynamicTest =
                    DynamicTest.dynamicTest(testName, () -> {
                        final TestIndex testIndex =
                                indexClass.getDeclaredConstructor(Map.class)
                                          .newInstance(new HashMap<>(Map.of(
                                                  "a", new TestSpecification[] {
                                                          new TestSpecification("a", "1", Map.of("c", "2", "d", "1")),
                                                          new TestSpecification("a", "2", Map.of("c", "1", "d", "2")),
                                                  },
                                                  "b", new TestSpecification[] {
                                                          new TestSpecification("b", "1", Map.of("c", "1", "d", "2")),
                                                          new TestSpecification("b", "2", Map.of("c", "2", "d", "1")),
                                                  },
                                                  "c", new TestSpecification[] {
                                                          new TestSpecification("c", "1", Map.of()),
                                                          new TestSpecification("c", "2", Map.of()),
                                                  },
                                                  "d", new TestSpecification[] {
                                                          new TestSpecification("d", "1", Map.of()),
                                                          new TestSpecification("d", "2", Map.of()),
                                                  })
                                          ));

                        final TestRequirement dep1 = new TestRequirement(new TestDependency("a"));
                        final TestRequirement dep2 = new TestRequirement(new TestDependency("b"));
                        final Set<TestRequirement> deps = Set.of(dep1, dep2);

                        final DependencyGraph<TestRequirement, TestRequirement> graph = new DependencyGraph<>();
                        final Resolution<TestRequirement, TestSpecification> resolver =
                                new Resolution<>(testIndex, new ConsoleUI(), deps, graph);

                        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> results = resolver.resolve();
                        final List<TestSpecification> testSpecs = results.getVertices()
                                                                         .values()
                                                                         .stream()
                                                                         .map(vertex -> vertex.getPayload()
                                                                                              .orElseThrow()
                                                                                              .getSpecification())
                                                                         .collect(Collectors.toList());
                        assertThat(testSpecs).satisfiesAnyOf(ts -> assertThat(testSpecs).containsExactlyInAnyOrder(
                                new TestSpecification("a", "2", Map.of("c", "1", "d", "2")),
                                new TestSpecification("b", "1", Map.of("c", "1", "d", "2")),
                                new TestSpecification("c", "1", Map.of()),
                                new TestSpecification("d", "2", Map.of())
                        ), ts -> assertThat(testSpecs).containsExactlyInAnyOrder(
                                new TestSpecification("a", "1", Map.of("c", "2", "d", "1")),
                                new TestSpecification("b", "2", Map.of("c", "2", "d", "1")),
                                new TestSpecification("c", "2", Map.of()),
                                new TestSpecification("d", "1", Map.of())
                        ));
                    });

            tests.add(dynamicTest);
        }

        return tests;
    }
}
