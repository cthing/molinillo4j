package org.cthing.molinillo;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;


public class PayloadTest {

    @Test
    public void testPossibilitySet() {
        final PossibilitySet<String, String> possibilitySet = new PossibilitySet<>(Set.of("abc"), List.of("def"));
        final Payload<String, String> payload = new Payload<>(possibilitySet);

        assertThat(payload.isPossibilitySet()).isTrue();
        assertThat(payload.isSpecification()).isFalse();
        assertThat(payload.getPossibilitySet()).isEqualTo(possibilitySet);
        assertThatIllegalStateException().isThrownBy(payload::getSpecification);
        assertThat(payload).hasToString("PossibilitySet { possibilities=def }");
    }

    @Test
    public void testSpecification() {
        final Payload<String, String> payload = new Payload<>("abc");

        assertThat(payload.isPossibilitySet()).isFalse();
        assertThat(payload.isSpecification()).isTrue();
        assertThat(payload.getSpecification()).isEqualTo("abc");
        assertThatIllegalStateException().isThrownBy(payload::getPossibilitySet);
        assertThat(payload).hasToString("abc");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(Payload.class)
                      .usingGetClass()
                      .verify();
    }
}
