package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SetPayloadTest {

    @Test
    public void testConstruction() {
        final SetPayload<String, String> payloadAction = new SetPayload<>("abc", "def");
        assertThat(payloadAction.getName()).isEqualTo("abc");
        assertThat(payloadAction.getPayload()).isEqualTo("def");
        assertThat(payloadAction).hasToString("SetPayload { name=abc, payload=def }");
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    public void testEquality() {
        final SetPayload<String, String> payloadAction1 = new SetPayload<>("abc", "def");
        final SetPayload<String, String> payloadAction2 = new SetPayload<>("abc", "def");
        final SetPayload<String, String> payloadAction3 = new SetPayload<>("xyx", "def");
        final SetPayload<String, String> payloadAction4 = new SetPayload<>("abc", "xyz");
        final SetPayload<Integer, String> payloadAction5 = new SetPayload<>("abc", 1234);

        assertThat(payloadAction1).isEqualTo(payloadAction2);
        assertThat(payloadAction1).hasSameHashCodeAs(payloadAction2);

        assertThat(payloadAction1).isNotEqualTo(payloadAction3);
        assertThat(payloadAction1).doesNotHaveSameHashCodeAs(payloadAction3);

        assertThat(payloadAction1).isNotEqualTo(payloadAction4);
        assertThat(payloadAction1).doesNotHaveSameHashCodeAs(payloadAction4);

        assertThat(payloadAction1).isNotEqualTo(payloadAction5);
        assertThat(payloadAction1).doesNotHaveSameHashCodeAs(payloadAction5);
    }

    @Test
    public void testUpDown() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "payload1");
        assertThat(vertex.getPayload()).isEqualTo("payload1");

        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        graph.getVertices().put(vertex.getName(), vertex);

        final SetPayload<String, String> payloadAction = new SetPayload<>("abc", "payload2");
        payloadAction.up(graph);
        assertThat(vertex.getPayload()).isEqualTo("payload2");
        payloadAction.down(graph);
        assertThat(vertex.getPayload()).isEqualTo("payload1");
    }
}
