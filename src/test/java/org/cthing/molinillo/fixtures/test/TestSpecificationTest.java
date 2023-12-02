package org.cthing.molinillo.fixtures.test;

import java.util.Map;

import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestSpecification;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;


public class TestSpecificationTest {

    @Test
    public void testConstruction() {
        final TestSpecification spec = new TestSpecification("spec1", "1.2.3",
                                                             Map.of("dep1", "< 2.0, >= 1.2.0", "dep2", "~> 3.1.0"));
        assertThat(spec.getName()).isEqualTo("spec1");
        assertThat(spec.getVersion().getOriginalVersion()).isEqualTo("1.2.3");
        assertThat(spec.isPreRelease()).isFalse();
        assertThat(spec.getDependencies()).containsExactlyInAnyOrder(
                new TestDependency("dep1", "< 2.0", ">= 1.2.0"),
                new TestDependency("dep2", "~> 3.1.0")
        );
        assertThat(spec).hasToString("spec1 (1.2.3)");
    }

    @Test
    public void testPreRelease() {
        final TestSpecification spec = new TestSpecification("spec2", "1.2.3.beta.1", Map.of());
        assertThat(spec.getName()).isEqualTo("spec2");
        assertThat(spec.getVersion().getOriginalVersion()).isEqualTo("1.2.3.beta.1");
        assertThat(spec.isPreRelease()).isTrue();
        assertThat(spec.getDependencies()).isEmpty();
        assertThat(spec).hasToString("spec2 (1.2.3.beta.1)");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(TestSpecification.class)
                      .usingGetClass()
                      .withPrefabValues(TestDependency.class,
                                        new TestDependency("dep1", "1.2.3"),
                                        new TestDependency("dep2", "4"))
                      .verify();
    }
}
