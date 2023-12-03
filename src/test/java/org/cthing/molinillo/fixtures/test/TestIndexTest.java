package org.cthing.molinillo.fixtures.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestIndex;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TestIndexTest {

    @Test
    public void testFromFixture() {
        final TestIndex index = TestIndex.fromFixture("restkit");
        final Map<String, TestSpecification[]> specs = index.getSpecs();
        assertThat(specs).hasSize(2);

        final TestSpecification spec1 = new TestSpecification("RestKit", "0.0.1", Map.of());
        final TestSpecification spec2 = new TestSpecification("RestKit", "0.0.2", Map.of());
        final TestSpecification spec3 = new TestSpecification("RestKit", "0.23.0", Map.of("RestKit/Core", "= 0.23.0"));
        final TestSpecification spec4 = new TestSpecification("RestKit", "0.23.1", Map.of("RestKit/Core", "= 0.23.1"));
        final TestSpecification spec5 = new TestSpecification("RestKit", "0.23.2", Map.of("RestKit/Core", "= 0.23.2"));
        final TestSpecification spec6 = new TestSpecification("RestKit", "0.23.3", Map.of("RestKit/Core", "= 0.23.3"));
        assertThat(specs.get("RestKit")).containsExactly(spec1, spec2, spec3, spec4, spec5, spec6);

        final TestSpecification spec7 = new TestSpecification("RestKit/Core", "0.23.0", Map.of());
        final TestSpecification spec8 = new TestSpecification("RestKit/Core", "0.23.1", Map.of());
        final TestSpecification spec9 = new TestSpecification("RestKit/Core", "0.23.2", Map.of());
        final TestSpecification spec10 = new TestSpecification("RestKit/Core", "0.23.3", Map.of());
        assertThat(specs.get("RestKit/Core")).containsExactly(spec7, spec8, spec9, spec10);
    }

    @Test
    public void testNameForDependency() {
        final TestIndex index = TestIndex.fromFixture("restkit");
        final TestRequirement requirement = new TestRequirement(new TestDependency("dep", "=1.2.3"));
        assertThat(index.nameForDependency(requirement)).isEqualTo("dep");
    }

    @Test
    public void testNameForSpecification() {
        final TestIndex index = TestIndex.fromFixture("restkit");
        final TestSpecification spec = new TestSpecification("spec", "1.2.3", Map.of());
        assertThat(index.nameForSpecification(spec)).isEqualTo("spec");
    }

    @Nested
    class RequirementsSatisfiedByTest {

        @Test
        public void testDependency() {
            final TestIndex index = TestIndex.fromFixture("restkit");
            final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> graph =
                    new DependencyGraph<>();
            final TestRequirement requirement =
                    new TestRequirement(new TestDependency("RestKit", ">=0.23.0", "<0.23.2"));
            final TestSpecification specification1 = new TestSpecification("RestKit", "0.23.1",
                                                                           Map.of("RestKit/Core", "= 0.23.1"));
            final TestSpecification specification2 = new TestSpecification("RestKit", "0.23.3",
                                                                           Map.of("RestKit/Core", "= 0.23.3"));
            assertThat(index.requirementSatisfiedBy(requirement, graph, specification1)).isTrue();
            assertThat(index.requirementSatisfiedBy(requirement, graph, specification2)).isFalse();
        }

        @Test
        public void testSpecification() {
            final TestIndex index = TestIndex.fromFixture("restkit");
            final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> graph =
                    new DependencyGraph<>();
            final TestRequirement requirement =
                    new TestRequirement(new TestSpecification("RestKit", "0.23.1", Map.of("RestKit/Core", "= 0.23.1")));
            final TestSpecification specification1 = new TestSpecification("RestKit", "0.23.1",
                                                                           Map.of("RestKit/Core", "= 0.23.1"));
            final TestSpecification specification2 = new TestSpecification("RestKit", "0.23.3",
                                                                           Map.of("RestKit/Core", "= 0.23.3"));
            assertThat(index.requirementSatisfiedBy(requirement, graph, specification1)).isTrue();
            assertThat(index.requirementSatisfiedBy(requirement, graph, specification2)).isFalse();
        }

        @Test
        public void testPreReleaseNotFound() {
            final TestIndex index = TestIndex.fromFixture("restkit");
            final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> graph =
                    new DependencyGraph<>();
            final TestSpecification graphSpecification = new TestSpecification("RestKit", "0.23.1",
                                                                          Map.of("RestKit/Core", "= 0.23.1"));
            final Payload<TestRequirement, TestSpecification> payload = Payload.fromSpecification(graphSpecification);
            graph.addVertex("RestKit", payload, false);
            final TestRequirement requirement =
                    new TestRequirement(new TestDependency("RestKit", ">=0.23.0", "<0.23.2"));
            final TestSpecification specification = new TestSpecification("RestKit", "0.23.1.alpha",
                                                                           Map.of("RestKit/Core", "= 0.23.1"));
            assertThat(index.requirementSatisfiedBy(requirement, graph, specification)).isFalse();
        }
    }

    @Test
    public void testSearchFor() {
        final TestIndex index = TestIndex.fromFixture("restkit");
        final TestRequirement requirement1 = new TestRequirement(new TestDependency("RestKit", ">=0.23.0", "<0.23.2"));
        final TestRequirement requirement2 = new TestRequirement(new TestDependency("RestKit", ">=0.24.0"));
        final TestRequirement requirement3 = new TestRequirement(new TestDependency("Rest", ">=1.0.0"));
        final TestSpecification spec1 = new TestSpecification("RestKit", "0.23.0", Map.of("RestKit/Core", "= 0.23.0"));
        final TestSpecification spec2 = new TestSpecification("RestKit", "0.23.1", Map.of("RestKit/Core", "= 0.23.1"));
        assertThat(index.searchFor(requirement1)).containsExactlyInAnyOrder(spec1, spec2);
        assertThat(index.searchFor(requirement2)).isEmpty();
        assertThat(index.searchFor(requirement3)).isEmpty();
    }

    @Test
    public void testDependenciesFor() {
        final TestIndex index = TestIndex.fromFixture("restkit");
        final TestSpecification spec = new TestSpecification("RestKit", "0.23.1", Map.of("RestKit/Core", "= 0.23.1"));
        final TestRequirement requirement = new TestRequirement(new TestDependency("RestKit/Core", "0.23.1"));
        assertThat(index.dependenciesFor(spec)).containsExactlyInAnyOrder(requirement);
    }

    @Test
    public void testSortDependencies() {
        final TestIndex index = TestIndex.fromFixture("awesome");
        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> graph =
                new DependencyGraph<>();
        final Payload<TestRequirement, TestSpecification> payload =
                Payload.fromSpecification(new TestSpecification("rack", "1.4.0", Map.of()));
        graph.addVertex("rack", payload, false);

        final TestSpecification specification = index.getSpecs().get("actionpack")[2];
        final List<TestRequirement> dependencies = new ArrayList<>(index.dependenciesFor(specification));
        final List<TestRequirement> sortedDependencies = index.sortDependencies(dependencies, graph, Map.of());
        final TestRequirement requirement1 = new TestRequirement(new TestDependency("activesupport", "= 2.3.5"));
        final TestRequirement requirement2 = new TestRequirement(new TestDependency("rack", "~> 1.0.0"));
        assertThat(sortedDependencies).containsExactly(requirement2, requirement1);
    }
}
