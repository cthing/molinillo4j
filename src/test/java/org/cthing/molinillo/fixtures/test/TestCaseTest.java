package org.cthing.molinillo.fixtures.test;

import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;
import org.cthing.molinillo.fixtures.TestCase;
import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestIndex;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TestCaseTest {

    @Test
    public void testFromFixture() {
        final TestCase testCase = TestCase.fromFixture("complex_conflict_unwinding");
        assertThat(testCase.getName()).isEqualTo("resolves a conflict which requires non-trivial unwinding");
        assertThat(testCase.getIndex().getSpecs()).hasSize(127);

        final TestRequirement requirement1 = new TestRequirement(new TestDependency("devise", ""));
        final TestRequirement requirement2 = new TestRequirement(new TestDependency("sprockets-rails", ""));
        final TestRequirement requirement3 = new TestRequirement(new TestDependency("rails", ""));
        final TestRequirement requirement4 = new TestRequirement(new TestDependency("spring", ""));
        final TestRequirement requirement5 = new TestRequirement(new TestDependency("web-console", ""));
        assertThat(testCase.getRequested()).containsExactlyInAnyOrder(requirement1, requirement2, requirement3,
                                                                      requirement4, requirement5);
        assertThat(testCase.getConflicts()).isEmpty();

        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> resultGraph = testCase.getResult();
        assertThat(resultGraph.getVertices()).hasSize(46);
        final DependencyGraph<TestRequirement, TestRequirement> baseGraph = testCase.getBase();
        assertThat(baseGraph.getVertices()).hasSize(8);
    }

    @Test
    public void testAll() {
        final TestCase[] testCases = TestCase.all();
        assertThat(testCases).hasSize(25);
    }

    @Test
    public void testResolve() {
        final TestCase testCase = TestCase.fromFixture("conflict_common_parent");
        final DependencyGraph<Payload<TestRequirement, TestSpecification>, TestRequirement> result =
                testCase.resolve(TestIndex.class);
        assertThat(result).isEqualTo(testCase.getResult());
    }
}
