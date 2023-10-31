package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

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
        final AddEdgeNoCircular<String, String> edgeAction1 = new AddEdgeNoCircular<>("start1", "end1", "req1");
        final AddEdgeNoCircular<String, String> edgeAction2 = new AddEdgeNoCircular<>("start1", "end1", "req1");
        final AddEdgeNoCircular<String, String> edgeAction3 = new AddEdgeNoCircular<>("start2", "end1", "req1");
        final AddEdgeNoCircular<String, String> edgeAction4 = new AddEdgeNoCircular<>("start1", "end2", "req1");
        final AddEdgeNoCircular<String, String> edgeAction5 = new AddEdgeNoCircular<>("start1", "end1", "req2");

        assertThat(edgeAction1).isEqualTo(edgeAction2);
        assertThat(edgeAction1).hasSameHashCodeAs(edgeAction2);

        assertThat(edgeAction1).isNotEqualTo(edgeAction3);
        assertThat(edgeAction1).doesNotHaveSameHashCodeAs(edgeAction3);
        assertThat(edgeAction1).isNotEqualTo(edgeAction4);
        assertThat(edgeAction1).doesNotHaveSameHashCodeAs(edgeAction4);
        assertThat(edgeAction1).isNotEqualTo(edgeAction5);
        assertThat(edgeAction1).doesNotHaveSameHashCodeAs(edgeAction5);
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
