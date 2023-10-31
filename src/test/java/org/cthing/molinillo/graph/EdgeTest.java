package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EdgeTest {

    @Test
    public void testConstruction() {
        final Vertex<String, String> origin = new Vertex<>("start", "payload1");
        final Vertex<String, String> destination = new Vertex<>("end", "payload2");
        final Edge<String, String> edge = new Edge<>(origin, destination, "req");
        assertThat(edge.getOrigin()).isEqualTo(origin);
        assertThat(edge.getDestination()).isEqualTo(destination);
        assertThat(edge.getRequirement()).isEqualTo("req");
        assertThat(edge).hasToString("Edge { start -> end (req) }");
    }

    @Test
    public void testEquality() {
        final Vertex<String, String> origin = new Vertex<>("start", "payload1");
        final Vertex<String, String> destination = new Vertex<>("end", "payload2");

        final Edge<String, String> edge1 = new Edge<>(origin, destination, "req");
        final Edge<String, String> edge2 = new Edge<>(origin, destination, "req");
        final Edge<String, String> edge3 = new Edge<>(origin, destination, "req2");
        final Edge<String, String> edge4 = new Edge<>(destination, origin, "req");

        assertThat(edge1).isEqualTo(edge2);
        assertThat(edge1).hasSameHashCodeAs(edge2);
        assertThat(edge1).isNotEqualTo(edge3);
        assertThat(edge1).doesNotHaveSameHashCodeAs(edge3);
        assertThat(edge1).isNotEqualTo(edge4);
        assertThat(edge1).doesNotHaveSameHashCodeAs(edge4);
    }
}
