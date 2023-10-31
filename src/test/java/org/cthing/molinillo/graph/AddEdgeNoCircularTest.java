package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class AddEdgeNoCircularTest {

    @Test
    public void testConstruction() {
        final AddEdgeNoCircular<String, String> edgeAction = new AddEdgeNoCircular<>("start", "end", "req");
        assertThat(edgeAction.getOrigin()).isEqualTo("start");
        assertThat(edgeAction.getDestination()).isEqualTo("end");
        assertThat(edgeAction.getRequirement()).isEqualTo("req");
        assertThat(edgeAction).hasToString("AddEdgeNoCircular { origin=start, destination=end, requirement=req }");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(AddEdgeNoCircular.class)
                      .usingGetClass()
                      .withPrefabValues(Action.class, new TestAction(), new TestAction())
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }

    @Test
    public void testUpDown() {
        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        final Vertex<String, String> origin = new Vertex<>("start", "payload1");
        final Vertex<String, String> destination = new Vertex<>("end", "payload2");
        graph.getVertices().put(origin.getName(), origin);
        graph.getVertices().put(destination.getName(), destination);

        final AddEdgeNoCircular<String, String> edgeAction = new AddEdgeNoCircular<>("start", "end", "req1");
        final Edge<String, String> edge = edgeAction.up(graph);
        assertThat(edge.getOrigin()).isEqualTo(origin);
        assertThat(edge.getDestination()).isEqualTo(destination);
        assertThat(origin.getOutgoingEdges()).containsExactly(edge);
        assertThat(destination.getIncomingEdges()).containsExactly(edge);

        edgeAction.down(graph);
        assertThat(origin.getOutgoingEdges()).isEmpty();
        assertThat(destination.getIncomingEdges()).isEmpty();
    }
}
