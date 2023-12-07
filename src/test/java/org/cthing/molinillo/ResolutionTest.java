package org.cthing.molinillo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cthing.molinillo.errors.CircularDependencyError;
import org.cthing.molinillo.errors.ResolverError;
import org.cthing.molinillo.errors.VersionConflictError;
import org.cthing.molinillo.fixtures.BerkshelfTestIndex;
import org.cthing.molinillo.fixtures.BundlerNoPenaltyTestIndex;
import org.cthing.molinillo.fixtures.BundlerTestIndex;
import org.cthing.molinillo.fixtures.CocoaPodsTestIndex;
import org.cthing.molinillo.fixtures.TestCase;
import org.cthing.molinillo.fixtures.TestIndex;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.fail;


public class ResolutionTest {

    private static final List<Class<? extends TestIndex>> INDEX_CLASSES = List.of(
            TestIndex.class,
            BundlerTestIndex.class,
            BundlerNoPenaltyTestIndex.class,
            BundlerReverseTestIndex.class,
            CocoaPodsTestIndex.class,
            BerkshelfTestIndex.class
    );

    @TestFactory
    public List<DynamicTest> resolveTestFactory() {
        final List<DynamicTest> tests = new ArrayList<>();
        for (final Class<? extends TestIndex> indexClass : INDEX_CLASSES) {
            for (final TestCase testCase : TestCase.all()) {
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
}
