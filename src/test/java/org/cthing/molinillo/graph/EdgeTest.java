package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

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
        EqualsVerifier.forClass(Edge.class)
                      .usingGetClass()
                      .withPrefabValues(Vertex.class, new Vertex<String, String>("abc", "def"),
                                        new Vertex<String, String>("lmn", "xyz"))
                      .verify();
    }
}
