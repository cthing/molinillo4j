package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class PossibilityStateTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testConstruction() {
        final String name = "abc";
        final List<String> requirements = List.of("req1", "req2");
        final DependencyGraph<Payload<String, String>, String> dependencyGraph = new DependencyGraph<>();
        dependencyGraph.addVertex("v1", new Payload<>("hello"), true);
        final String requirement = "def";
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(Set.of("dep1"), List.of("pos1"));
        final List<PossibilitySet<String, String>> possilibilites = List.of(possibilitySet);
        final int depth = 2;
        final Conflict<String, String> conflict = mock(Conflict.class);
        final Map<String, Conflict<String, String>> conflicts = Map.of("conflict1", conflict);
        final UnwindDetails<String, String> unwindDetails = mock(UnwindDetails.class);
        final List<UnwindDetails<String, String>> unwinds = List.of(unwindDetails);
        final PossibilityState<String, String> possibilityState = new PossibilityState<>(name, requirements,
                                                                                         dependencyGraph, requirement,
                                                                                         possibilitySet, depth,
                                                                                         conflicts, unwinds);

        assertThat(possibilityState.getName()).isEqualTo(name);
        assertThat(possibilityState.getRequirements()).isEqualTo(requirements);
        assertThat(possibilityState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(possibilityState.getRequirement()).contains(requirement);
        assertThat(possibilityState.getPossibilities()).isEqualTo(possilibilites);
        assertThat(possibilityState.getDepth()).isEqualTo(depth);
        assertThat(possibilityState.getConflicts()).isEqualTo(conflicts);
        assertThat(possibilityState.getUnusedUnwindOptions()).isEqualTo(unwinds);
    }
}
