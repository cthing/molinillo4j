package org.cthing.molinillo;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class UnwindDetailsTest {

    @Test
    public void testConstruction() {
        final int stateIndex = 3;
        final String stateRequirement = "req2";
        final List<String> requirementTree = List.of("req3");
        final List<String> conflictingRequirements = List.of("req4");
        final List<List<String>> requirementTrees = List.of(List.of("req5", "req6"), List.of("req7"));
        final List<String> requirementsUnwoundToInstead = List.of("req8");
        final UnwindDetails<String, String> details = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                          requirementTree, conflictingRequirements,
                                                                          requirementTrees,
                                                                          requirementsUnwoundToInstead);
        assertThat(details.getStateIndex()).isEqualTo(stateIndex);
        assertThat(details.getStateRequirement()).isEqualTo(stateRequirement);
        assertThat(details.getRequirementTree()).isEqualTo(requirementTree);
        assertThat(details.getConflictingRequirements()).isEqualTo(conflictingRequirements);
        assertThat(details.getRequirementTrees()).isEqualTo(requirementTrees);
        assertThat(details.getRequirementsUnwoundToInstead()).isEqualTo(requirementsUnwoundToInstead);
        assertThat(details).hasToString("UnwindDetails { stateIndex=3, stateRequirement=req2, requirementTree=[req3],"
                                                + " conflictingRequirements=[req4], requirementTrees=[[req5, req6],"
                                                + " [req7]], requirementsUnwoundToInstead=[req8] }");
    }

    @Nested
    class ReveresedRequirementTreeIndexTest {

        @Test
        public void testNoStateRequirement() {
            final int stateIndex = 3;
            final List<String> requirementTree = List.of("req3");
            final List<String> conflictingRequirements = List.of();
            final List<List<String>> requirementTrees = List.of();
            final List<String> requirementsUnwoundToInstead = List.of();

            final UnwindDetails<String, String> details1 = new UnwindDetails<>(stateIndex, null,
                                                                              requirementTree, conflictingRequirements,
                                                                              requirementTrees,
                                                                              requirementsUnwoundToInstead);
            assertThat(details1.reversedRequirementTreeIndex()).isEqualTo(-1);
        }

        @Test
        public void testStateRequirementFound() {
            final int stateIndex = 3;
            final String stateRequirement = "req2";
            final List<String> requirementTree = List.of("req3", "req2", "req18", "req17");
            final List<String> conflictingRequirements = List.of();
            final List<List<String>> requirementTrees = List.of();
            final List<String> requirementsUnwoundToInstead = List.of();

            final UnwindDetails<String, String> details1 = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                              requirementTree, conflictingRequirements,
                                                                              requirementTrees,
                                                                              requirementsUnwoundToInstead);
            assertThat(details1.reversedRequirementTreeIndex()).isEqualTo(2);
        }

        @Test
        public void testStateRequirementNotFound() {
            final int stateIndex = 3;
            final String stateRequirement = "req2";
            final List<String> requirementTree = List.of("req3", "req18", "req17");
            final List<String> conflictingRequirements = List.of();
            final List<List<String>> requirementTrees = List.of();
            final List<String> requirementsUnwoundToInstead = List.of();

            final UnwindDetails<String, String> details1 = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                              requirementTree, conflictingRequirements,
                                                                              requirementTrees,
                                                                              requirementsUnwoundToInstead);
            assertThat(details1.reversedRequirementTreeIndex()).isEqualTo(-1);
        }
    }

    @Nested
    class UnwindingToPrimaryRequirementTest {

        @Test
        public void testIsPrimary() {
            final int stateIndex = 3;
            final String stateRequirement = "req2";
            final List<String> requirementTree = List.of("req3", "req18", "req17", "req2");
            final List<String> conflictingRequirements = List.of();
            final List<List<String>> requirementTrees = List.of();
            final List<String> requirementsUnwoundToInstead = List.of();

            final UnwindDetails<String, String> details = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                              requirementTree, conflictingRequirements,
                                                                              requirementTrees,
                                                                              requirementsUnwoundToInstead);
            assertThat(details.unwindingToPrimaryRequirement()).isTrue();
        }

        @Test
        public void testIsNotPrimary() {
            final int stateIndex = 3;
            final String stateRequirement = "req2";
            final List<String> requirementTree = List.of("req3", "req18", "req17");
            final List<String> conflictingRequirements = List.of();
            final List<List<String>> requirementTrees = List.of();
            final List<String> requirementsUnwoundToInstead = List.of();

            final UnwindDetails<String, String> details = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                              requirementTree, conflictingRequirements,
                                                                              requirementTrees,
                                                                              requirementsUnwoundToInstead);
            assertThat(details.unwindingToPrimaryRequirement()).isFalse();
        }
    }

    @Test
    public void testSubDependenciesToAvoid() {
        final int stateIndex = 3;
        final String stateRequirement = "req2";
        final List<String> requirementTree = List.of();
        final List<String> conflictingRequirements = List.of();
        final List<List<String>> requirementTrees = List.of(List.of("req5", "req6"),
                                                            List.of("req7", "req2"),
                                                            List.of("req19", "req2", "req20"),
                                                            List.of("req12", "req2", "req16"));
        final List<String> requirementsUnwoundToInstead = List.of();

        final UnwindDetails<String, String> details = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                          requirementTree, conflictingRequirements,
                                                                          requirementTrees,
                                                                          requirementsUnwoundToInstead);
        assertThat(details.subDependenciesToAvoid()).containsExactly("req20", "req16");
    }

    @Test
    public void testAllRequirements() {
        final int stateIndex = 3;
        final String stateRequirement = "req2";
        final List<String> requirementTree = List.of();
        final List<String> conflictingRequirements = List.of();
        final List<List<String>> requirementTrees = List.of(List.of("req5", "req6"),
                                                            List.of("req7", "req2"),
                                                            List.of("req19", "req2", "req20"),
                                                            List.of("req12", "req2", "req16"));
        final List<String> requirementsUnwoundToInstead = List.of();

        final UnwindDetails<String, String> details = new UnwindDetails<>(stateIndex, stateRequirement,
                                                                          requirementTree, conflictingRequirements,
                                                                          requirementTrees,
                                                                          requirementsUnwoundToInstead);
        assertThat(details.allRequirements()).containsExactly("req5", "req6",
                                                              "req7", "req2",
                                                              "req19", "req2", "req20",
                                                              "req12", "req2", "req16");

    }

    @Test
    public void testOrdering() {
        final UnwindDetails<String, String> details1 = new UnwindDetails<>(3, null, List.of(), List.of(), List.of(),
                                                                           List.of());
        final UnwindDetails<String, String> details2 = new UnwindDetails<>(1, null, List.of(), List.of(), List.of(),
                                                                           List.of());
        assertThat(details1.compareTo(details2)).isEqualTo(1);
        assertThat(details2.compareTo(details1)).isEqualTo(-1);

        final String stateRequirement3 = "req2";
        final List<String> requirementTree3 = List.of("req3", "req2", "req18", "req17");
        final UnwindDetails<String, String> details3 = new UnwindDetails<>(3, stateRequirement3, requirementTree3,
                                                                           List.of(), List.of(), List.of());
        final String stateRequirement4 = "req2";
        final List<String> requirementTree4 = List.of("req3", "req2", "req18");
        final UnwindDetails<String, String> details4 = new UnwindDetails<>(3, stateRequirement4, requirementTree4,
                                                                           List.of(), List.of(), List.of());
        final UnwindDetails<String, String> details5 = new UnwindDetails<>(3, stateRequirement4, requirementTree4,
                                                                           List.of(), List.of(), List.of());
        assertThat(details3.compareTo(details4)).isGreaterThan(0);
        assertThat(details4.compareTo(details3)).isLessThan(0);
        assertThat(details4.compareTo(details5)).isZero();

    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(UnwindDetails.class)
                      .usingGetClass()
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED, Warning.NONFINAL_FIELDS)
                      .verify();
    }
}
