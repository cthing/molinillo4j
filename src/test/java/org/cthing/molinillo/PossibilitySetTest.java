package org.cthing.molinillo;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;


public class PossibilitySetTest {

    @Test
    public void testConstruction() {
        final Set<String> dependencies = Set.of("dep1", "dep2", "dep3");
        final List<String> possibilites = List.of("pos1", "pos2");
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(dependencies, possibilites);

        assertThat(possibilitySet.getDependencies()).isEqualTo(dependencies);
        assertThat(possibilitySet.getPossibilities()).isEqualTo(possibilites);
        assertThat(possibilitySet.getLatestVersion()).isEqualTo("pos2");
        assertThat(possibilitySet).hasToString("PossibilitySet { possibilities=pos1, pos2 }");
    }

    @Test
    public void testEmpty() {
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(Set.of(), List.of());
        assertThat(possibilitySet.getDependencies()).isEmpty();
        assertThat(possibilitySet.getPossibilities()).isEmpty();
        assertThat(possibilitySet.getLatestVersion()).isNull();
        assertThat(possibilitySet).hasToString("PossibilitySet { possibilities= }");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(PossibilitySet.class)
                      .usingGetClass()
                      .verify();
    }
}
