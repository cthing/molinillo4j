package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class AddVertexTest {

    @Test
    public void testConstruction() {
        AddVertex<String, String> vertexAction = new AddVertex<>("abc", "payload1", false);
        assertThat(vertexAction.getName()).isEqualTo("abc");
        assertThat(vertexAction.getPayload()).contains("payload1");
        assertThat(vertexAction.isRoot()).isFalse();
        assertThat(vertexAction).hasToString("AddVertex { name=abc, payload=payload1, root=false }");

        vertexAction = new AddVertex<>("def", "payload2", true);
        assertThat(vertexAction.getName()).isEqualTo("def");
        assertThat(vertexAction.getPayload()).contains("payload2");
        assertThat(vertexAction.isRoot()).isTrue();
        assertThat(vertexAction).hasToString("AddVertex { name=def, payload=payload2, root=true }");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(AddVertex.class)
                      .usingGetClass()
                      .withPrefabValues(Action.class, new TestAction(), new TestAction())
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }

    @Test
    public void testUpDownNew() {
        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        assertThat(graph.vertexNamed("abc")).isNull();

        final AddVertex<String, String> vertexAction = new AddVertex<>("abc", "payload", true);

        final Vertex<String, String> vertex = vertexAction.up(graph);
        assertThat(vertexAction.getName()).isEqualTo("abc");
        assertThat(vertexAction.getPayload()).contains("payload");
        assertThat(vertexAction.isRoot()).isTrue();
        assertThat(graph.vertexNamed("abc")).isEqualTo(vertex);

        vertexAction.down(graph);
        assertThat(graph.vertexNamed("abc")).isNull();
    }

    @Test
    public void testUpDownExisting() {
        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        final Vertex<String, String> vertex = new Vertex<>("abc", "payload1");
        vertex.setRoot(false);
        graph.getVertices().put(vertex.getName(), vertex);

        final AddVertex<String, String> vertexAction = new AddVertex<>("abc", "payload2", true);
        final Vertex<String, String> vertex2 = vertexAction.up(graph);
        assertThat(vertex2.getName()).isEqualTo("abc");
        assertThat(vertex2.getPayload()).contains("payload1");
        assertThat(vertex2.isRoot()).isTrue();
        assertThat(graph.vertexNamed("abc")).isEqualTo(vertex2);

        vertexAction.down(graph);
        final Vertex<String, String> vertex3 = graph.vertexNamed("abc");
        assertThat(vertex3).isNotNull();
        assertThat(vertex3.getName()).isEqualTo("abc");
        assertThat(vertex3.getPayload()).contains("payload1");
        assertThat(vertex3.isRoot()).isFalse();
    }
}
