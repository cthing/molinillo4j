package org.cthing.molinillo.errors;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class NoSuchDependencyErrorTest {

    @Test
    public void testDependents() {
        final NoSuchDependencyError error = new NoSuchDependencyError("pkgA");
        assertThat(error).hasMessage("Unable to find a specification for pkgA");
    }

    @Test
    public void testWithDependents() {
        final NoSuchDependencyError error = new NoSuchDependencyError("pkgA", Set.of("pkgB", "pkgC"));
        assertThat(error).hasMessage("Unable to find a specification for pkgA depended upon by pkgB and pkgC");
    }
}
