package org.cthing.molinillo.graph;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class VertexTest {

    @Test
    public void testConstruction() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "def");
        assertThat(vertex.getName()).isEqualTo("abc");
        assertThat(vertex.getPayload()).contains("def");
        assertThat(vertex.isRoot()).isFalse();
        assertThat(vertex.getExplicitRequirements()).isEmpty();
        assertThat(vertex.getOutgoingEdges()).isEmpty();
        assertThat(vertex.getIncomingEdges()).isEmpty();
        assertThat(vertex).hasToString("abc");
    }

    @Test
    public void testName() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "def");
        vertex.setName("xyz");
        assertThat(vertex.getName()).isEqualTo("xyz");
    }

    @Test
    public void testPayload() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "def");
        vertex.setPayload("xyz");
        assertThat(vertex.getPayload()).contains("xyz");
    }

    @Test
    public void testRoot() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "def");
        vertex.setRoot(true);
        assertThat(vertex.isRoot()).isTrue();
    }

    @Test
    public void testRequirements() {
        final Vertex<String, String> vertex1 = new Vertex<>("v1", "v1 payload");
        assertThat(vertex1.requirements()).isEmpty();

        vertex1.getExplicitRequirements().add("req1");
        assertThat(vertex1.requirements()).containsExactlyInAnyOrder("req1");

        final Vertex<String, String> vertex2 = new Vertex<>("v2", "v2 payload");
        final Edge<String, String> edge = new Edge<>(vertex2, vertex1, "req2");
        vertex1.getIncomingEdges().add(edge);
        assertThat(vertex1.requirements()).containsExactlyInAnyOrder("req1", "req2");
    }

    @Test
    public void testPredecessors() {
        final Vertex<String, String> vertex1 = new Vertex<>("v1", "v1 payload");
        final Vertex<String, String> vertex2 = new Vertex<>("v2", "v2 payload");
        final Vertex<String, String> vertex3 = new Vertex<>("v3", "v3 payload");
        final Edge<String, String> edge1 = new Edge<>(vertex2, vertex1, "req1");
        final Edge<String, String> edge2 = new Edge<>(vertex3, vertex1, "req2");
        vertex1.getIncomingEdges().add(edge1);
        vertex1.getIncomingEdges().add(edge2);
        assertThat(vertex1.predecessors()).containsExactlyInAnyOrder(vertex2, vertex3);
    }

    @Test
    public void testSuccessors() {
        final Vertex<String, String> vertex1 = new Vertex<>("v1", "v1 payload");
        final Vertex<String, String> vertex2 = new Vertex<>("v2", "v2 payload");
        final Vertex<String, String> vertex3 = new Vertex<>("v3", "v3 payload");
        final Edge<String, String> edge1 = new Edge<>(vertex1, vertex2, "req1");
        final Edge<String, String> edge2 = new Edge<>(vertex1, vertex3, "req2");
        vertex1.getOutgoingEdges().add(edge1);
        vertex1.getOutgoingEdges().add(edge2);
        assertThat(vertex1.successors()).containsExactlyInAnyOrder(vertex2, vertex3);
    }

    @Test
    public void testPathTo() {
        final Vertex<String, String> vertex1 = new Vertex<>("v1", "v1 payload");
        final Vertex<String, String> vertex2 = new Vertex<>("v2", "v2 payload");
        final Vertex<String, String> vertex3 = new Vertex<>("v3", "v3 payload");
        final Vertex<String, String> vertex4 = new Vertex<>("v4", "v4 payload");

        final Edge<String, String> edge1 = new Edge<>(vertex1, vertex2, "req1");
        final Edge<String, String> edge2 = new Edge<>(vertex2, vertex3, "req2");

        vertex1.getOutgoingEdges().add(edge1);
        vertex2.getIncomingEdges().add(edge1);

        vertex2.getOutgoingEdges().add(edge2);
        vertex3.getIncomingEdges().add(edge2);

        assertThat(vertex1.pathTo(vertex1)).isTrue();
        assertThat(vertex1.pathTo(vertex2)).isTrue();
        assertThat(vertex1.pathTo(vertex3)).isTrue();

        assertThat(vertex1.pathTo(vertex4)).isFalse();
        assertThat(vertex2.pathTo(vertex1)).isFalse();
        assertThat(vertex3.pathTo(vertex1)).isFalse();

        final Edge<String, String> edge3 = new Edge<>(vertex3, vertex1, "req3");
        vertex3.getOutgoingEdges().add(edge3);
        vertex1.getIncomingEdges().add(edge3);
        assertThat(vertex3.pathTo(vertex1)).isTrue();
        assertThat(vertex2.pathTo(vertex1)).isTrue();
        assertThat(vertex1.pathTo(vertex4)).isFalse();

        final Edge<String, String> edge4 = new Edge<>(vertex4, vertex4, "req4");
        vertex4.getOutgoingEdges().add(edge4);
        vertex4.getIncomingEdges().add(edge4);
        assertThat(vertex4.pathTo(vertex4)).isTrue();
    }

    @Test
    public void testEquality() {
        final Vertex<String, String> vertex1 = new Vertex<>("abc", "payload1");
        final Vertex<String, String> vertex2 = new Vertex<>("abc", "payload1");
        final Vertex<String, String> vertex3 = new Vertex<>("def", "payload1");
        final Vertex<String, String> vertex4 = new Vertex<>("def", "payload2");

        assertThat(vertex1).isEqualTo(vertex2);
        assertThat(vertex1).isNotEqualTo(vertex3);
        assertThat(vertex1).isNotEqualTo(vertex4);

        assertThat(vertex1).hasSameHashCodeAs(vertex2);
        assertThat(vertex1).doesNotHaveSameHashCodeAs(vertex3);
        assertThat(vertex1).doesNotHaveSameHashCodeAs(vertex4);

        final Vertex<String, String> vertex5 = new Vertex<>("abc", "payload1");
        final Vertex<String, String> vertex6 = new Vertex<>("abc", "payload1");
        final Edge<String, String> edge1 = new Edge<>(vertex5, vertex1, "req1");
        final Edge<String, String> edge2 = new Edge<>(vertex6, vertex1, "req1");
        vertex5.getOutgoingEdges().add(edge1);
        vertex6.getOutgoingEdges().add(edge2);
        assertThat(vertex5).isEqualTo(vertex6);

        final Vertex<String, String> vertex7 = new Vertex<>("abc", "payload1");
        final Vertex<String, String> vertex8 = new Vertex<>("abc", "payload1");
        final Edge<String, String> edge3 = new Edge<>(vertex7, vertex1, "req1");
        final Edge<String, String> edge4 = new Edge<>(vertex8, vertex3, "req1");
        vertex7.getOutgoingEdges().add(edge3);
        vertex8.getOutgoingEdges().add(edge4);
        assertThat(vertex7).isNotEqualTo(vertex8);
    }
}
