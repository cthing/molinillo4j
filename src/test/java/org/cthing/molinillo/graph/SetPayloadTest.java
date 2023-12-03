package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class SetPayloadTest {

    @Test
    public void testConstruction() {
        final SetPayload<String, String> payloadAction = new SetPayload<>("abc", "def");
        assertThat(payloadAction.getName()).isEqualTo("abc");
        assertThat(payloadAction.getPayload()).contains("def");
        assertThat(payloadAction).hasToString("SetPayload { name=abc, payload=def }");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(SetPayload.class)
                      .usingGetClass()
                      .withPrefabValues(Action.class, new TestAction(), new TestAction())
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }

    @Test
    public void testUpDown() {
        final Vertex<String, String> vertex = new Vertex<>("abc", "payload1");
        assertThat(vertex.getPayload()).contains("payload1");

        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        graph.getVertices().put(vertex.getName(), vertex);

        final SetPayload<String, String> payloadAction = new SetPayload<>("abc", "payload2");
        payloadAction.up(graph);
        assertThat(vertex.getPayload()).contains("payload2");
        payloadAction.down(graph);
        assertThat(vertex.getPayload()).contains("payload1");
    }
}
