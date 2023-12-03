package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;


public class DependencyStateTest {

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
        final DependencyState<String, String> dependencyState = new DependencyState<>(name, requirements,
                                                                                      dependencyGraph, requirement,
                                                                                      possilibilites, depth,
                                                                                      conflicts, unwinds);

        assertThat(dependencyState.getName()).isEqualTo(name);
        assertThat(dependencyState.getRequirements()).isEqualTo(requirements);
        assertThat(dependencyState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(dependencyState.getRequirement()).isEqualTo(requirement);
        assertThat(dependencyState.getPossibilities()).isEqualTo(possilibilites);
        assertThat(dependencyState.getDepth()).isEqualTo(depth);
        assertThat(dependencyState.getConflicts()).isEqualTo(conflicts);
        assertThat(dependencyState.getUnusedUnwindOptions()).isEqualTo(unwinds);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPopPossibilityState() {
        final String name = "abc";
        final List<String> requirements = List.of("req1", "req2");
        final DependencyGraph<Payload<String, String>, String> dependencyGraph = new DependencyGraph<>();
        dependencyGraph.addVertex("v1", new Payload<>("hello"), true);
        final String requirement = "def";
        final PossibilitySet<String, String> possibilitySet1 = new PossibilitySet<>(Set.of("dep1"), List.of("pos1"));
        final PossibilitySet<String, String> possibilitySet2 = new PossibilitySet<>(Set.of("dep2"), List.of("pos2"));
        final List<PossibilitySet<String, String>> possilibilites = List.of(possibilitySet1, possibilitySet2);
        final int depth = 2;
        final Conflict<String, String> conflict = mock(Conflict.class);
        final Map<String, Conflict<String, String>> conflicts = Map.of("conflict1", conflict);
        final UnwindDetails<String, String> unwindDetails = mock(UnwindDetails.class);
        final List<UnwindDetails<String, String>> unwinds = List.of(unwindDetails);
        final DependencyState<String, String> dependencyState = new DependencyState<>(name, requirements,
                                                                                      dependencyGraph, requirement,
                                                                                      possilibilites, depth,
                                                                                      conflicts, unwinds);
        final PossibilityState<String, String> possibilityState = dependencyState.popPossibilityState();

        assertThat(possibilityState.getName()).isEqualTo(name);
        assertThat(possibilityState.getRequirements()).isEqualTo(requirements);
        assertThat(possibilityState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(possibilityState.getRequirement()).isEqualTo(requirement);
        assertThat(possibilityState.getPossibilities()).isEqualTo(List.of(possibilitySet2));
        assertThat(possibilityState.getDepth()).isEqualTo(depth + 1);
        assertThat(possibilityState.getConflicts()).isEqualTo(conflicts);
        assertThat(possibilityState.getUnusedUnwindOptions()).isEqualTo(unwinds);

        assertThat(dependencyState.getName()).isEqualTo(name);
        assertThat(dependencyState.getRequirements()).isEqualTo(requirements);
        assertThat(dependencyState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(dependencyState.getRequirement()).isEqualTo(requirement);
        assertThat(dependencyState.getPossibilities()).isEqualTo(List.of(possibilitySet1));
        assertThat(dependencyState.getDepth()).isEqualTo(depth);
        assertThat(dependencyState.getConflicts()).isEqualTo(conflicts);
        assertThat(dependencyState.getUnusedUnwindOptions()).isEqualTo(unwinds);

        assertThatNoException().isThrownBy(() -> possibilityState.getActivated().rewindTo(possibilityState));
        assertThatIllegalStateException().isThrownBy(() -> possibilityState.getActivated().rewindTo(possibilityState));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyPopPossibilityState() {
        final String name = "abc";
        final List<String> requirements = List.of("req1", "req2");
        final DependencyGraph<Payload<String, String>, String> dependencyGraph = new DependencyGraph<>();
        dependencyGraph.addVertex("v1", new Payload<>("hello"), true);
        final String requirement = "def";
        final List<PossibilitySet<String, String>> possilibilites = List.of();
        final int depth = 2;
        final Conflict<String, String> conflict = mock(Conflict.class);
        final Map<String, Conflict<String, String>> conflicts = Map.of("conflict1", conflict);
        final UnwindDetails<String, String> unwindDetails = mock(UnwindDetails.class);
        final List<UnwindDetails<String, String>> unwinds = List.of(unwindDetails);
        final DependencyState<String, String> dependencyState = new DependencyState<>(name, requirements,
                                                                                      dependencyGraph, requirement,
                                                                                      possilibilites, depth,
                                                                                      conflicts, unwinds);
        final PossibilityState<String, String> possibilityState = dependencyState.popPossibilityState();

        assertThat(possibilityState.getName()).isEqualTo(name);
        assertThat(possibilityState.getRequirements()).isEqualTo(requirements);
        assertThat(possibilityState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(possibilityState.getRequirement()).isEqualTo(requirement);
        assertThat(possibilityState.getPossibilities()).isEmpty();
        assertThat(possibilityState.getDepth()).isEqualTo(depth + 1);
        assertThat(possibilityState.getConflicts()).isEqualTo(conflicts);
        assertThat(possibilityState.getUnusedUnwindOptions()).isEqualTo(unwinds);

        assertThat(dependencyState.getName()).isEqualTo(name);
        assertThat(dependencyState.getRequirements()).isEqualTo(requirements);
        assertThat(dependencyState.getActivated()).isEqualTo(dependencyGraph);
        assertThat(dependencyState.getRequirement()).isEqualTo(requirement);
        assertThat(dependencyState.getPossibilities()).isEmpty();
        assertThat(dependencyState.getDepth()).isEqualTo(depth);
        assertThat(dependencyState.getConflicts()).isEqualTo(conflicts);
        assertThat(dependencyState.getUnusedUnwindOptions()).isEqualTo(unwinds);

        assertThatNoException().isThrownBy(() -> possibilityState.getActivated().rewindTo(possibilityState));
        assertThatIllegalStateException().isThrownBy(() -> possibilityState.getActivated().rewindTo(possibilityState));
    }
}
