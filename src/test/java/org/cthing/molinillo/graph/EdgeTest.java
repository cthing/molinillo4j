package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EdgeTest {

    @Test
    public void testToString() {
        final Vertex<String, String> origin = new Vertex<>("start", "payload1");
        final Vertex<String, String> destination = new Vertex<>("end", "payload2");
        final Edge<String, String> edge = new Edge<>(origin, destination, "req");
        assertThat(edge).hasToString("Edge { start -> end (req) }");
    }
}
