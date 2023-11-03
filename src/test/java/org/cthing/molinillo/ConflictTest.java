package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;


public class ConflictTest {

    @Test
    public void testConstruction() {
        final String requirement = "req1";
        final Map<String, Set<String>> requirements = Map.of("abc", Set.of("req2", "req3"));
        final String existing = "spec1";
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(Set.of("dep1"), List.of("pos1",
                                                                                                           "pos2"));
        final String lockedRequirement = "req17";
        final List<List<String>> requirementTrees = List.of(List.of("req23", "req24"), List.of("req68"));
        final Map<String, String> activatedByName = Map.of("xyz", "lmn");
        final RuntimeException underlyingError = new RuntimeException();
        final Conflict<String, String> conflict = new Conflict<>(requirement, requirements, existing, possibilitySet,
                                                                 lockedRequirement, requirementTrees, activatedByName,
                                                                 underlyingError);
        assertThat(conflict.getRequirement()).isEqualTo(requirement);
        assertThat(conflict.getRequirements()).isEqualTo(requirements);
        assertThat(conflict.getExisting()).isEqualTo(existing);
        assertThat(conflict.getPossibilitySet()).isEqualTo(possibilitySet);
        assertThat(conflict.getLockedRequirement()).isEqualTo(lockedRequirement);
        assertThat(conflict.getRequirementTrees()).isEqualTo(requirementTrees);
        assertThat(conflict.getActivatedByName()).isEqualTo(activatedByName);
        assertThat(conflict.getUnderlyingError()).isEqualTo(underlyingError);
        assertThat(conflict.getPossibility()).isEqualTo("pos2");
        assertThat(conflict.toString()).contains("Conflict { requirement=req1");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(Conflict.class)
                      .usingGetClass()
                      .verify();
    }
}
