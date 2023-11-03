package org.cthing.molinillo.errors;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class NoSuchDependencyErrorTest {

    @Test
    public void testConstruction() {
        final NoSuchDependencyError error1 = new NoSuchDependencyError("dep1");
        assertThat((String)error1.getDependency()).isEqualTo("dep1");
        assertThat(error1.getRequiredBy()).isEmpty();
        assertThat(error1.getMessage()).isEqualTo("Unable to find a specification for dep1");

        final NoSuchDependencyError error2 = new NoSuchDependencyError("dep2", Set.of("req1", "req2"));
        assertThat((String)error2.getDependency()).isEqualTo("dep2");
        assertThat(error2.getRequiredBy()).containsExactlyInAnyOrder("req1", "req2");
        assertThat(error2.getMessage()).isEqualTo("Unable to find a specification for dep2 depended upon by req1 and req2");
    }
}
