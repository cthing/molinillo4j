package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

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
        final Tag<String, String> tag1 = new Tag<>("abc");
        final Tag<String, String> tag2 = new Tag<>("abc");
        final Tag<String, String> tag3 = new Tag<>("def");
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        final Tag<String, String> tag4 = new Tag<>(obj1);
        final Tag<String, String> tag5 = new Tag<>(obj1);
        final Tag<String, String> tag6 = new Tag<>(obj2);

        assertThat(tag1).isEqualTo(tag1);

        assertThat(tag1).isEqualTo(tag2);
        assertThat(tag1).hasSameHashCodeAs(tag2);

        assertThat(tag1).isNotEqualTo(tag3);
        assertThat(tag1).doesNotHaveSameHashCodeAs(tag3);

        assertThat(tag4).isEqualTo(tag4);

        assertThat(tag4).isEqualTo(tag5);
        assertThat(tag4).hasSameHashCodeAs(tag5);

        assertThat(tag4).isNotEqualTo(tag6);
        assertThat(tag4).doesNotHaveSameHashCodeAs(tag6);
    }
}
