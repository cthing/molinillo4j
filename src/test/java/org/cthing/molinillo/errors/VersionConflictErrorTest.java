package org.cthing.molinillo.errors;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.SpecificationProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class VersionConflictErrorTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testConstruction() {
        final String requirement = "req1";
        final Map<String, Set<String>> requirements = Map.of("abc", Set.of("req2", "req3"),
                                                             "def", Set.of("req4"));
        final Conflict<String, String> conflict = new Conflict<>(requirement, requirements, null, null, null,
                                                                  List.of(), Map.of(), null);
        final Map<String, Conflict<String, String>> conflicts = Map.of("foo", conflict);
        final SpecificationProvider<String, String> specificationProvider = mock(SpecificationProvider.class);
        final VersionConflictError error = new VersionConflictError(conflicts, specificationProvider);

        assertThat(error.getConflicts()).isEqualTo(conflicts);
        assertThat(error.getSpecificationProvider()).isEqualTo(specificationProvider);
        assertThat(error.toString()).contains("org.cthing.molinillo.errors.VersionConflictError: "
                                                      + "Unable to satisfy the following requirements:");
    }
}
