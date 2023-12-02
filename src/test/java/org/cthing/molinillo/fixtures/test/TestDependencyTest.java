package org.cthing.molinillo.fixtures.test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.versionparser.Version;
import org.cthing.versionparser.VersionParsingException;
import org.cthing.versionparser.gem.GemVersionScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


public class TestDependencyTest {

    @Test
    public void testConstruction() {
        final TestDependency dependency = new TestDependency("test1", "~> 1.0", ">= 1.4.0");
        assertThat(dependency.getName()).isEqualTo("test1");
        assertThat(dependency.getVersionConstraint().toString()).isEqualTo("[1.4.0,2.ZZZ)");
        assertThat(dependency.isPreRelease()).isFalse();
        assertThat(dependency).hasToString("test1 ([1.4.0,2.ZZZ))");
    }

    static Stream<Arguments> prereleaseProvider() {
        return Stream.of(
                arguments(false, ""),
                arguments(false, "1.0.0"),
                arguments(false, "=1.0.0"),
                arguments(false, ">1.0.0"),
                arguments(false, ">=1.0.0"),
                arguments(false, "<1.0.0"),
                arguments(false, "<=1.0.0"),
                arguments(false, "~>1.0.0"),
                arguments(false, ">1.0.0", "<2.0.0"),
                arguments(false, ">1.0.0", "<2.0.0.alpha1"),
                arguments(false, ">1.0.0", "<2.0.0", ">1.5.0"),
                arguments(true, "1.0.0.alpha"),
                arguments(true, "=1.0.0.alpha"),
                arguments(true, "=1.0.0.alpha"),
                arguments(true, ">1.0.0.alpha"),
                arguments(true, ">=1.0.0.alpha"),
                arguments(true, "<1.0.0.alpha"),
                arguments(true, "<=1.0.0.alpha"),
                arguments(true, "~>1.0.0.alpha"),
                arguments(true, ">1.0.0.alpha", "<2.0.0"),
                arguments(true, ">1.0.0", "<=2.0.0.alpha1"),
                arguments(true, ">1.0.0", "<=3.0.0", ">2.0.0.alpha"),
                arguments(true, ">1.0.0", "<=7.0.0", ">1.4.0", "<=4.0.0.alpha")
        );
    }

    @ParameterizedTest
    @MethodSource("prereleaseProvider")
    public void testPreRelease(final ArgumentsAccessor accessor) {
        final TestDependency dependency = new TestDependency("test", IntStream.range(1, accessor.size())
                                                                              .mapToObj(accessor::getString)
                                                                              .toArray(String[]::new));
        assertThat(dependency.isPreRelease()).isEqualTo(accessor.getBoolean(0));
    }

    @Test
    public void testEquality() throws VersionParsingException {
        final Version version1 = GemVersionScheme.parseVersion("1.0");
        final Version version2 = GemVersionScheme.parseVersion("3.0");
        EqualsVerifier.forClass(TestDependency.class)
                      .usingGetClass()
                      .withPrefabValues(Version.class, version1, version2)
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }

    @Test
    public void testOrdering() {
        final TestDependency dependency1 = new TestDependency("def", "1.0.0");
        final TestDependency dependency2 = new TestDependency("xyz", "1.0.0");
        final TestDependency dependency3 = new TestDependency("abc", "5.0.0");
        final TestDependency dependency4 = new TestDependency("abc", "6.0.0");
        assertThat(dependency1).isGreaterThan(dependency3);
        assertThat(dependency1).isLessThan(dependency2);
        assertThat(dependency3).isEqualByComparingTo(dependency4);
    }
}
