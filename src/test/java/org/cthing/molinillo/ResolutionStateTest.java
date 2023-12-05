package org.cthing.molinillo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class ResolutionStateTest {

    private final ResolutionState<String, String> resolutionState = new ResolutionState<>();

    @Test
    public void testEmpty() {
        assertThat(this.resolutionState.getName()).isEmpty();
        assertThat(this.resolutionState.getRequirements()).isEmpty();
        assertThat(this.resolutionState.getActivated()).isEqualTo(new DependencyGraph<Payload<String, String>, String>());
        assertThat(this.resolutionState.getRequirement()).isNull();
        assertThat(this.resolutionState.getPossibilities()).isEmpty();
        assertThat(this.resolutionState.getDepth()).isZero();
        assertThat(this.resolutionState.getConflicts()).isEmpty();
        assertThat(this.resolutionState.getUnusedUnwindOptions()).isEmpty();
    }

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
        final ResolutionState<String, String> resolutionState1 = new ResolutionState<>(name, requirements,
                                                                                       dependencyGraph, requirement,
                                                                                       possilibilites, depth,
                                                                                       conflicts, unwinds);

        assertThat(resolutionState1.getName()).isEqualTo(name);
        assertThat(resolutionState1.getRequirements()).isEqualTo(requirements);
        assertThat(resolutionState1.getActivated()).isEqualTo(dependencyGraph);
        assertThat(resolutionState1.getRequirement()).isEqualTo(requirement);
        assertThat(resolutionState1.getPossibilities()).isEqualTo(possilibilites);
        assertThat(resolutionState1.getDepth()).isEqualTo(depth);
        assertThat(resolutionState1.getConflicts()).isEqualTo(conflicts);
        assertThat(resolutionState1.getUnusedUnwindOptions()).isEqualTo(unwinds);
    }

    @Test
    public void testName() {
        this.resolutionState.setName("xyz");
        assertThat(this.resolutionState.getName()).isEqualTo("xyz");
    }

    @Test
    public void testRequirements() {
        final List<String> requirements = List.of("req1", "req2");
        this.resolutionState.setRequirements(requirements);
        assertThat(this.resolutionState.getRequirements()).isEqualTo(requirements);
    }

    @Test
    public void testActivated() {
        final DependencyGraph<Payload<String, String>, String> dependencyGraph = new DependencyGraph<>();
        dependencyGraph.addVertex("v1", new Payload<>("hello"), true);
        this.resolutionState.setActivated(dependencyGraph);
        assertThat(this.resolutionState.getActivated()).isEqualTo(dependencyGraph);
    }

    @Test
    public void testRequirement() {
        final String requirement = "def";
        this.resolutionState.setRequirement(requirement);
        assertThat(this.resolutionState.getRequirement()).isEqualTo(requirement);
    }

    @Test
    public void testPossibilities() {
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(Set.of("dep1"), List.of("pos1"));
        final List<PossibilitySet<String, String>> possilibilites = List.of(possibilitySet);
        this.resolutionState.getPossibilities().addAll(possilibilites);
        assertThat(this.resolutionState.getPossibilities()).isEqualTo(possilibilites);
    }

    @Test
    public void testDepth() {
        final int depth = 17;
        this.resolutionState.setDepth(depth);
        assertThat(this.resolutionState.getDepth()).isEqualTo(depth);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConflicts() {
        final Conflict<String, String> conflict = mock(Conflict.class);
        final Map<String, Conflict<String, String>> conflicts = Map.of("conflict1", conflict);
        this.resolutionState.setConflicts(conflicts);
        assertThat(this.resolutionState.getConflicts()).isEqualTo(conflicts);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUnusedUnwindOptions() {
        final UnwindDetails<String, String> unwindDetails = mock(UnwindDetails.class);
        final List<UnwindDetails<String, String>> unwinds = List.of(unwindDetails);
        this.resolutionState.setUnusedUnwindOptions(unwinds);
        assertThat(this.resolutionState.getUnusedUnwindOptions()).isEqualTo(unwinds);
    }
}
