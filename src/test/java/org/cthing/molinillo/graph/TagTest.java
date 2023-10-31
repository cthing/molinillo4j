package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class TagTest {

    @Test
    public void testConstruction() {
        final Tag<String, String> tag1 = new Tag<>("abc");
        assertThat(tag1.getValue()).isEqualTo("abc".hashCode());
        assertThat(tag1).hasToString("Tag { value=" + "abc".hashCode() + " }");

        final Integer value = 1234;
        final Tag<String, String> tag2 = new Tag<>(value);
        assertThat(tag2).hasToString("Tag { value=" + System.identityHashCode(value) + " }");
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void testEquality() {
        EqualsVerifier.forClass(Tag.class)
                      .usingGetClass()
                      .withPrefabValues(Action.class, new TestAction(), new TestAction())
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }
}
