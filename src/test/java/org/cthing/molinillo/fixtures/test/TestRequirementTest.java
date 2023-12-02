package org.cthing.molinillo.fixtures.test;

import java.util.Map;

import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;


public class TestRequirementTest {

    @Test
    public void testFromDependency() {
        final TestDependency dependency = new TestDependency("dep", ">=1.0", "<2.0");
        final TestRequirement requirement = TestRequirement.fromDependency(dependency);
        assertThat(requirement.getName()).isEqualTo("dep");
        assertThat(requirement.isDependency()).isTrue();
        assertThat(requirement.isSpecification()).isFalse();
        assertThat(requirement.getDependency()).isEqualTo(dependency);
        assertThat(requirement.isPreRelease()).isFalse();
        assertThat(requirement).hasToString("dep ([1.0,2.0))");
    }

    @Test
    public void testFromSpecification() {
        final TestSpecification spec = new TestSpecification("spec", "1.2.3",
                                                             Map.of("dep1", "< 2.0, >= 1.2.0", "dep2", "~> 3.1.0"));
        final TestRequirement requirement = TestRequirement.fromSpecification(spec);
        assertThat(requirement.getName()).isEqualTo("spec");
        assertThat(requirement.isDependency()).isFalse();
        assertThat(requirement.isSpecification()).isTrue();
        assertThat(requirement.getSpecification()).isEqualTo(spec);
        assertThat(requirement.isPreRelease()).isFalse();
        assertThat(requirement).hasToString("spec (1.2.3)");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(TestRequirement.class)
                      .usingGetClass()
                      .withPrefabValues(TestDependency.class,
                                        new TestDependency("dep1", "1.2.3"),
                                        new TestDependency("dep2", "4"))
                      .verify();
    }
}
