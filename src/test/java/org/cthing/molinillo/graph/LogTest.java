package org.cthing.molinillo.graph;

import java.util.List;

import org.cthing.molinillo.DependencyGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class LogTest {

    private final Log<String, String> log = new Log<>();
    private final DependencyGraph<String, String> graph = new DependencyGraph<>(this.log);

    @Test
    public void testEmptyGraph() {
        assertThat(this.graph.isEmpty()).isTrue();
        this.log.tag(this.graph, "tag1");
        assertThat(this.graph.isEmpty()).isTrue();
        this.log.rewindTo(this.graph, "tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testAddVertex() {
        this.log.tag(this.graph, "tag1");

        final Vertex<String, String> vertex1 = this.log.addVertex(this.graph, "vertex1", "payload1", false);
        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.isEmpty()).isFalse();

        this.log.tag(this.graph, "tag2");

        final Vertex<String, String> vertex2 = this.log.addVertex(this.graph, "vertex2", "payload2", false);
        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.vertexNamed("vertex2")).isEqualTo(vertex2);
        assertThat(this.graph.isEmpty()).isFalse();

        this.log.rewindTo(this.graph, "tag2");

        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.vertexNamed("vertex2")).isNull();
        assertThat(this.graph.isEmpty()).isFalse();

        this.log.rewindTo(this.graph, "tag1");

        assertThat(this.graph.vertexNamed("vertex1")).isNull();
        assertThat(this.graph.vertexNamed("vertex2")).isNull();
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testAddEdgeNoCircular() {
        this.log.tag(this.graph, "tag1");

        final Vertex<String, String> vertex1 = this.log.addVertex(this.graph, "vertex1", "payload1", false);
        final Vertex<String, String> vertex2 = this.log.addVertex(this.graph, "vertex2", "payload2", false);
        final Vertex<String, String> vertex3 = this.log.addVertex(this.graph, "vertex3", "payload3", false);

        this.log.tag(this.graph, "tag2");

        final Edge<String, String> edge1 = this.log.addEdgeNoCircular(this.graph, "vertex1", "vertex2", "req");
        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactly(edge1);
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).isEmpty();

        this.log.tag(this.graph, "tag3");

        final Edge<String, String> edge2 = this.log.addEdgeNoCircular(this.graph, "vertex1", "vertex3", "req");
        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactlyInAnyOrder(edge1, edge2);
        assertThat(vertex3.getIncomingEdges()).containsExactly(edge2);
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.log.tag(this.graph, "tag4");

        final Edge<String, String> edge3 = this.log.addEdgeNoCircular(this.graph, "vertex2", "vertex3", "req");
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).containsExactlyInAnyOrder(edge3);
        assertThat(vertex3.getIncomingEdges()).containsExactly(edge2, edge3);
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.log.rewindTo(this.graph, "tag3");

        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactly(edge1);
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).isEmpty();
        assertThat(vertex3.getIncomingEdges()).isEmpty();
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.log.rewindTo(this.graph, "tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testDetachVertexNamed() {
        this.log.tag(this.graph, "tag1");

        final Vertex<String, String> vertexA = this.log.addVertex(this.graph, "A", "payloadA", true);
        final Vertex<String, String> vertexB = this.log.addVertex(this.graph, "B", "payloadB", false);
        final Vertex<String, String> vertexC = this.log.addVertex(this.graph, "C", "payloadC", false);
        final Vertex<String, String> vertexD = this.log.addVertex(this.graph, "D", "payloadD", false);
        final Vertex<String, String> vertexE = this.log.addVertex(this.graph, "E", "payloadE", true);

        this.log.tag(this.graph, "tag2");

        final Edge<String, String> edgeAB = this.log.addEdgeNoCircular(this.graph, "A", "B", "");
        final Edge<String, String> edgeAC = this.log.addEdgeNoCircular(this.graph, "A", "C", "");
        final Edge<String, String> edgeBC = this.log.addEdgeNoCircular(this.graph, "B", "C", "");
        final Edge<String, String> edgeCD = this.log.addEdgeNoCircular(this.graph, "C", "D", "");
        final Edge<String, String> edgeCE = this.log.addEdgeNoCircular(this.graph, "C", "E", "");

        this.log.tag(this.graph, "tag3");

        final List<Vertex<String, String>> removedVertices = this.log.detachVertexNamed(this.graph, "C");
        assertThat(removedVertices).containsExactlyInAnyOrder(vertexC, vertexD);
        assertThat(this.graph.vertexNamed("A")).isEqualTo(vertexA);
        assertThat(this.graph.vertexNamed("B")).isEqualTo(vertexB);
        assertThat(this.graph.vertexNamed("E")).isEqualTo(vertexE);
        assertThat(this.graph.vertexNamed("C")).isNull();
        assertThat(this.graph.vertexNamed("D")).isNull();
        assertThat(vertexA.getIncomingEdges()).isEmpty();
        assertThat(vertexA.getOutgoingEdges()).containsExactly(edgeAB);
        assertThat(vertexB.getIncomingEdges()).containsExactly(edgeAB);
        assertThat(vertexB.getOutgoingEdges()).isEmpty();
        assertThat(vertexE.getIncomingEdges()).isEmpty();
        assertThat(vertexE.getOutgoingEdges()).isEmpty();

        this.log.rewindTo(this.graph, "tag3");

        assertThat(this.graph.vertexNamed("A")).isEqualTo(vertexA);
        assertThat(this.graph.vertexNamed("B")).isEqualTo(vertexB);
        assertThat(this.graph.vertexNamed("C")).isEqualTo(vertexC);
        assertThat(this.graph.vertexNamed("D")).isEqualTo(vertexD);
        assertThat(this.graph.vertexNamed("E")).isEqualTo(vertexE);
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

        this.log.rewindTo(this.graph, "tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testSetPayload() {
        this.log.tag(this.graph, "tag1");

        final Vertex<String, String> vertexA = this.log.addVertex(this.graph, "A", "payloadA", true);
        assertThat(vertexA.getPayload()).isEqualTo("payloadA");

        this.log.tag(this.graph, "tag2");

        this.log.setPayload(this.graph, "A", "payload17");
        assertThat(vertexA.getPayload()).isEqualTo("payload17");

        this.log.rewindTo(this.graph, "tag2");
        assertThat(vertexA.getPayload()).isEqualTo("payloadA");

        this.log.rewindTo(this.graph, "tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testLogReplay() {
        this.log.addVertex(this.graph, "vertex1", "payload1", false);
        this.log.addVertex(this.graph, "vertex2", "payload2", false);
        this.log.addVertex(this.graph, "vertex3", "payload3", false);

        this.log.addEdgeNoCircular(this.graph, "vertex1", "vertex2", "req");
        this.log.addEdgeNoCircular(this.graph, "vertex1", "vertex3", "req");
        this.log.addEdgeNoCircular(this.graph, "vertex2", "vertex3", "req");

        final DependencyGraph<String, String> copy = new DependencyGraph<>();
        this.log.forEach(action -> action.up(copy));
        assertThat(copy).isEqualTo(this.graph);
    }
}
