package org.cthing.molinillo.fixtures.test;

import java.util.List;

import org.cthing.molinillo.BundlerReverseTestIndex;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.fixtures.TestCase;
import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TestCaseTest {

    @Test
    public void testFromFixture() {
        final TestCase testCase = TestCase.fromFixture("complex_conflict_unwinding");
        assertThat(testCase.getName()).isEqualTo("resolves a conflict which requires non-trivial unwinding");
        assertThat(testCase.getIndex().getSpecs()).hasSize(127);

        final TestDependency dependency1 = new TestDependency("devise", "");
        final TestDependency dependency2 = new TestDependency("sprockets-rails", "");
        final TestDependency dependency3 = new TestDependency("rails", "");
        final TestDependency dependency4 = new TestDependency("spring", "");
        final TestDependency dependency5 = new TestDependency("web-console", "");
        assertThat(testCase.getRequested()).containsExactlyInAnyOrder(dependency1, dependency2, dependency3,
                                                                      dependency4, dependency5);
        assertThat(testCase.getConflicts()).isEmpty();

        final DependencyGraph<TestSpecification, TestDependency> resultGraph = testCase.getResult();
        assertThat(resultGraph.getVertices()).hasSize(46);
        final DependencyGraph<TestDependency, TestDependency> baseGraph = testCase.getBase();
        assertThat(baseGraph.getVertices()).hasSize(8);
    }

    @Test
    public void testAll() {
        final List<TestCase> testCases = TestCase.all();
        assertThat(testCases).hasSize(25);
    }

    @Test
    public void testResolve() {
        final TestCase testCase = TestCase.fromFixture("complex_conflict");
        final DependencyGraph<TestSpecification, TestDependency> result =
                testCase.resolve(BundlerReverseTestIndex.class);
        assertThat(result).isEqualTo(testCase.getResult());
    }
}
