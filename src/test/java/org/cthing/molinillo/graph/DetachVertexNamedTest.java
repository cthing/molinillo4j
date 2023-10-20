package org.cthing.molinillo.graph;

import java.util.List;
import java.util.Map;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


public class DetachVertexNamedTest {

    @Test
    public void testConstruction() {
        final DetachVertexNamed<String, String> detachAction = new DetachVertexNamed<>("abc");
        assertThat(detachAction.getName()).isEqualTo("abc");
        assertThat(detachAction).hasToString("DetachVertexNamed { name=abc }");
    }

    @Test
    public void testEquality() {
        final DetachVertexNamed<String, String> detachAction1 = new DetachVertexNamed<>("abc");
        final DetachVertexNamed<String, String> detachAction2 = new DetachVertexNamed<>("abc");
        final DetachVertexNamed<String, String> detachAction3 = new DetachVertexNamed<>("def");

        assertThat(detachAction1).isEqualTo(detachAction2);
        assertThat(detachAction1).hasSameHashCodeAs(detachAction2);
        assertThat(detachAction1).isNotEqualTo(detachAction3);
        assertThat(detachAction1).doesNotHaveSameHashCodeAs(detachAction3);
    }

    @Test
    public void testUpDown() {
        final Vertex<String, String> vertexA = new Vertex<>("A", "payloadA");
        vertexA.setRoot(true);
        final Vertex<String, String> vertexB = new Vertex<>("B", "payloadB");
        vertexB.setRoot(false);
        final Vertex<String, String> vertexC = new Vertex<>("C", "payloadC");
        vertexC.setRoot(false);
        final Vertex<String, String> vertexD = new Vertex<>("D", "payloadD");
        vertexD.setRoot(false);
        final Vertex<String, String> vertexE = new Vertex<>("E", "payloadE");
        vertexE.setRoot(true);

        final DependencyGraph<String, String> graph = new DependencyGraph<>();
        graph.getVertices().putAll(Map.of(vertexA.getName(), vertexA,
                                          vertexB.getName(), vertexB,
                                          vertexC.getName(), vertexC,
                                          vertexD.getName(), vertexD,
                                          vertexE.getName(), vertexE));
        final Edge<String, String> edgeAB =
                new AddEdgeNoCircular<String, String>(vertexA.getName(), vertexB.getName(), "").up(graph);
        final Edge<String, String> edgeAC =
                new AddEdgeNoCircular<String, String>(vertexA.getName(), vertexC.getName(), "").up(graph);
        final Edge<String, String> edgeBC =
                new AddEdgeNoCircular<String, String>(vertexB.getName(), vertexC.getName(), "").up(graph);
        final Edge<String, String> edgeCD =
                new AddEdgeNoCircular<String, String>(vertexC.getName(), vertexD.getName(), "").up(graph);
        final Edge<String, String> edgeCE =
                new AddEdgeNoCircular<String, String>(vertexC.getName(), vertexE.getName(), "").up(graph);

        final DetachVertexNamed<String, String> detachAction = new DetachVertexNamed<>(vertexC.getName());

        final List<Vertex<String, String>> removedVertices = detachAction.up(graph);
        assertThat(removedVertices).containsExactlyInAnyOrder(vertexC, vertexD);
        assertThat(graph.getVertices()).contains(entry(vertexA.getName(), vertexA));
        assertThat(graph.getVertices()).contains(entry(vertexB.getName(), vertexB));
        assertThat(graph.getVertices()).contains(entry(vertexE.getName(), vertexE));
        assertThat(graph.getVertices()).doesNotContain(entry(vertexC.getName(), vertexC));
        assertThat(graph.getVertices()).doesNotContain(entry(vertexD.getName(), vertexD));
        assertThat(vertexA.getIncomingEdges()).isEmpty();
        assertThat(vertexA.getOutgoingEdges()).containsExactly(edgeAB);
        assertThat(vertexB.getIncomingEdges()).containsExactly(edgeAB);
        assertThat(vertexB.getOutgoingEdges()).isEmpty();
        assertThat(vertexE.getIncomingEdges()).isEmpty();
        assertThat(vertexE.getOutgoingEdges()).isEmpty();

        // Note that the deletion of vertex C resulted in the deletion of vertex D because it was orphaned.
        // Vertex E was not deleted because it was marked as a root vertex. The deletion of the orphaned vertex
        // D was done recursively through the dependency graph, which meant it generated an action in the log.
        // Therefore, to fully restore the state, the original (not logged action) needed to be undone and the
        // log needed to be popped to undo the second action. This is why cyclic coupling across three classes
        // leads to testing challenges (in this case the need to interact with the action log).
        detachAction.down(graph);
        graph.rewindLast();
        assertThat(graph.getVertices()).contains(entry(vertexA.getName(), vertexA));
        assertThat(graph.getVertices()).contains(entry(vertexB.getName(), vertexB));
        assertThat(graph.getVertices()).contains(entry(vertexC.getName(), vertexC));
        assertThat(graph.getVertices()).contains(entry(vertexD.getName(), vertexD));
        assertThat(graph.getVertices()).contains(entry(vertexE.getName(), vertexE));
        assertThat(vertexA.getIncomingEdges()).isEmpty();
        assertThat(vertexA.getOutgoingEdges()).containsExactlyInAnyOrder(edgeAB, edgeAC);
        assertThat(vertexB.getIncomingEdges()).containsExactly(edgeAB);
        assertThat(vertexB.getOutgoingEdges()).containsExactlyInAnyOrder(edgeBC);
        assertThat(vertexC.getIncomingEdges()).containsExactlyInAnyOrder(edgeAC, edgeBC);
        assertThat(vertexC.getOutgoingEdges()).containsExactlyInAnyOrder(edgeCD, edgeCE);
        assertThat(vertexD.getIncomingEdges()).containsExactlyInAnyOrder(edgeCD);
        assertThat(vertexD.getOutgoingEdges()).isEmpty();
        assertThat(vertexE.getIncomingEdges()).containsExactlyInAnyOrder(edgeCE);
        assertThat(vertexE.getOutgoingEdges()).isEmpty();
    }
}
