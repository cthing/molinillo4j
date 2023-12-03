package org.cthing.molinillo;

import java.util.List;

import org.cthing.molinillo.errors.CircularDependencyError;
import org.cthing.molinillo.graph.Action;
import org.cthing.molinillo.graph.Edge;
import org.cthing.molinillo.graph.TestAction;
import org.cthing.molinillo.graph.Vertex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


public class DependencyGraphTest {

    private final DependencyGraph<String, String> graph = new DependencyGraph<>();

    @Test
    public void testConstruction() {
        assertThat(this.graph.getVertices()).isEmpty();
        assertThat(this.graph.isEmpty()).isTrue();
        assertThat(this.graph).hasToString("DependencyGraph { vertices=0 }");
    }

    @Test
    public void testEmptyGraph() {
        assertThat(this.graph.isEmpty()).isTrue();
        this.graph.tag("tag1");
        assertThat(this.graph.isEmpty()).isTrue();
        this.graph.rewindTo("tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testAddVertex() {
        this.graph.tag("tag1");

        final Vertex<String, String> vertex1 = this.graph.addVertex("vertex1", "payload1", false);
        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.isEmpty()).isFalse();

        this.graph.tag("tag2");

        final Vertex<String, String> vertex2 = this.graph.addVertex("vertex2", "payload2", false);
        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.vertexNamed("vertex2")).isEqualTo(vertex2);
        assertThat(this.graph.isEmpty()).isFalse();

        this.graph.rewindTo("tag2");

        assertThat(this.graph.vertexNamed("vertex1")).isEqualTo(vertex1);
        assertThat(this.graph.vertexNamed("vertex2")).isNull();
        assertThat(this.graph.isEmpty()).isFalse();

        this.graph.rewindTo("tag1");

        assertThat(this.graph.vertexNamed("vertex1")).isNull();
        assertThat(this.graph.vertexNamed("vertex2")).isNull();
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testAddEdgeWithoutCycle() throws CircularDependencyError {
        this.graph.tag("tag1");

        final Vertex<String, String> vertex1 = this.graph.addVertex("vertex1", "payload1", false);
        final Vertex<String, String> vertex2 = this.graph.addVertex("vertex2", "payload2", false);
        final Vertex<String, String> vertex3 = this.graph.addVertex("vertex3", "payload3", false);

        this.graph.tag("tag2");

        final Edge<String, String> edge1 = this.graph.addEdge(vertex1, vertex2, "req");
        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactly(edge1);
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).isEmpty();

        this.graph.tag("tag3");

        final Edge<String, String> edge2 = this.graph.addEdge(vertex1, vertex3, "req");
        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactlyInAnyOrder(edge1, edge2);
        assertThat(vertex3.getIncomingEdges()).containsExactly(edge2);
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.graph.tag("tag4");

        final Edge<String, String> edge3 = this.graph.addEdge(vertex2, vertex3, "req");
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).containsExactly(edge3);
        assertThat(vertex3.getIncomingEdges()).containsExactlyInAnyOrder(edge2, edge3);
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.graph.rewindTo("tag3");

        assertThat(vertex1.getIncomingEdges()).isEmpty();
        assertThat(vertex1.getOutgoingEdges()).containsExactly(edge1);
        assertThat(vertex2.getIncomingEdges()).containsExactly(edge1);
        assertThat(vertex2.getOutgoingEdges()).isEmpty();
        assertThat(vertex3.getIncomingEdges()).isEmpty();
        assertThat(vertex3.getOutgoingEdges()).isEmpty();

        this.graph.rewindTo("tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testAddEdgeWithCycle() throws CircularDependencyError {
        final Vertex<String, String> vertex1 = this.graph.addVertex("vertex1", "payload1", false);
        final Vertex<String, String> vertex2 = this.graph.addVertex("vertex2", "payload2", false);
        final Vertex<String, String> vertex3 = this.graph.addVertex("vertex3", "payload3", false);
        this.graph.addEdge(vertex1, vertex2, "req");
        this.graph.addEdge(vertex2, vertex3, "req");
        assertThatExceptionOfType(CircularDependencyError.class)
                .isThrownBy(() -> this.graph.addEdge(vertex3, vertex1, "req"))
                .withMessage("There is a circular dependency between vertex1 and vertex2 and vertex3");
    }

    @Test
    public void testAddChildVertex() throws CircularDependencyError {
        this.graph.tag("tag1");

        final Vertex<String, String> vertex1 = this.graph.addVertex("vertex1", "payload1", false);
        final Vertex<String, String> vertex2 = this.graph.addVertex("vertex2", "payload2", false);

        this.graph.tag("tag2");

        final Vertex<String, String> vertex3 = this.graph.addChildVertex("vertex3", "payload3",
                                                                         List.of("vertex1", "vertex2"), "");
        assertThat(this.graph.vertexNamed("vertex3")).isEqualTo(vertex3);
        assertThat(vertex3.getIncomingEdges()).containsExactlyInAnyOrder(new Edge<>(vertex1, vertex3, ""),
                                                                         new Edge<>(vertex2, vertex3, ""));
        assertThat(vertex1.getOutgoingEdges()).containsExactly(new Edge<>(vertex1, vertex3, ""));
        assertThat(vertex2.getOutgoingEdges()).containsExactly(new Edge<>(vertex2, vertex3, ""));

        assertThatIllegalArgumentException().isThrownBy(() -> this.graph.addChildVertex("vertex3", "payload3",
                                                                                        List.of(), ""));
    }

    @Test
    public void testDetachVertexNamed() throws CircularDependencyError {
        this.graph.tag("tag1");

        final Vertex<String, String> vertexA = this.graph.addVertex("A", "payloadA", true);
        final Vertex<String, String> vertexB = this.graph.addVertex("B", "payloadB", false);
        final Vertex<String, String> vertexC = this.graph.addVertex("C", "payloadC", false);
        final Vertex<String, String> vertexD = this.graph.addVertex("D", "payloadD", false);
        final Vertex<String, String> vertexE = this.graph.addVertex("E", "payloadE", true);

        this.graph.tag("tag2");

        final Edge<String, String> edgeAB = this.graph.addEdge(vertexA, vertexB, "");
        final Edge<String, String> edgeAC = this.graph.addEdge(vertexA, vertexC, "");
        final Edge<String, String> edgeBC = this.graph.addEdge(vertexB, vertexC, "");
        final Edge<String, String> edgeCD = this.graph.addEdge(vertexC, vertexD, "");
        final Edge<String, String> edgeCE = this.graph.addEdge(vertexC, vertexE, "");

        this.graph.tag("tag3");

        final List<Vertex<String, String>> removedVertices = this.graph.detachVertexNamed("C");
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

        this.graph.rewindTo("tag3");

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

        this.graph.rewindTo("tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testSetPayload() {
        this.graph.tag("tag1");

        final Vertex<String, String> vertexA = this.graph.addVertex("A", "payloadA", true);
        assertThat(vertexA.getPayload()).contains("payloadA");

        this.graph.tag("tag2");

        this.graph.setPayload("A", "payload17");
        assertThat(vertexA.getPayload()).contains("payload17");

        this.graph.rewindTo("tag2");
        assertThat(vertexA.getPayload()).contains("payloadA");

        this.graph.rewindTo("tag1");
        assertThat(this.graph.isEmpty()).isTrue();
    }

    @Test
    public void testToDot() throws CircularDependencyError {
        final Vertex<String, String> vertex1 = this.graph.addVertex("A", "p1", false);
        final Vertex<String, String> vertex2 = this.graph.addVertex("B", "p2", false);
        final Vertex<String, String> vertex3 = this.graph.addVertex("C", "p3", false);
        this.graph.addEdge(vertex1, vertex2, "req");
        this.graph.addEdge(vertex2, vertex3, "req");

        assertThat(this.graph.toDot()).isEqualTo("""
                                                 digraph G {
                                                 A [label="{A|p1}"]
                                                 B [label="{B|p2}"]
                                                 C [label="{C|p3}"]
                                                   A -> B [label=req]
                                                   B -> C [label=req]}
                                                 """);
    }

    @Test
    public void testToString() throws CircularDependencyError {
        final Vertex<String, String> vertex1 = this.graph.addVertex("A", "p1", false);
        final Vertex<String, String> vertex2 = this.graph.addVertex("B", "p2", false);
        final Vertex<String, String> vertex3 = this.graph.addVertex("C", "p3", false);
        this.graph.addEdge(vertex1, vertex2, "req");
        this.graph.addEdge(vertex2, vertex3, "req");

        assertThat(this.graph).hasToString("DependencyGraph { vertices=3 }");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(DependencyGraph.class)
                      .usingGetClass()
                      .withPrefabValues(Action.class, new TestAction(), new TestAction())
                      .withPrefabValues(Vertex.class, new Vertex<String, String>("abc", "def"),
                                        new Vertex<String, String>("lmn", "xyz"))
                      .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }

    @Nested
    class PathTest {

        @Test
        public void testSimple() throws CircularDependencyError {
            final Vertex<String, String> vertex1 = DependencyGraphTest.this.graph.addVertex("v1", "p1", false);
            final Vertex<String, String> vertex2 = DependencyGraphTest.this.graph.addVertex("v2", "p2", false);
            final Vertex<String, String> vertex3 = DependencyGraphTest.this.graph.addVertex("v3", "p3", false);
            DependencyGraphTest.this.graph.addEdge(vertex1, vertex2, "req");
            DependencyGraphTest.this.graph.addEdge(vertex2, vertex3, "req");

            final List<Vertex<String, String>> path = DependencyGraphTest.this.graph.path(vertex1, vertex3);
            assertThat(path).containsExactly(vertex1, vertex2, vertex3);
        }

        @Test
        public void testCycle() throws CircularDependencyError {
            final Vertex<String, String> vertex1 = DependencyGraphTest.this.graph.addVertex("v1", "p1", false);
            final Vertex<String, String> vertex2 = DependencyGraphTest.this.graph.addVertex("v2", "p2", false);
            final Vertex<String, String> vertex3 = DependencyGraphTest.this.graph.addVertex("v3", "p3", false);
            DependencyGraphTest.this.graph.addEdge(vertex1, vertex2, "req");
            DependencyGraphTest.this.graph.addEdge(vertex2, vertex3, "req");

            final Edge<String, String> edge = new Edge<>(vertex3, vertex1, "");
            vertex3.getOutgoingEdges().add(edge);
            vertex1.getIncomingEdges().add(edge);

            List<Vertex<String, String>> path = DependencyGraphTest.this.graph.path(vertex1, vertex3);
            assertThat(path).containsExactly(vertex1, vertex2, vertex3);
            path = DependencyGraphTest.this.graph.path(vertex2, vertex1);
            assertThat(path).containsExactly(vertex2, vertex3, vertex1);
        }

        @Test
        public void testEmpty() {
            final Vertex<String, String> vertex1 = new Vertex<>("v1", "");
            final Vertex<String, String> vertex2 = new Vertex<>("v2", "");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> DependencyGraphTest.this.graph.path(vertex1, vertex2))
                    .withMessage("There is no path from v1 to v2");
        }

        @Test
        public void testNone() {
            final Vertex<String, String> vertex1 = DependencyGraphTest.this.graph.addVertex("v1", "p1", false);
            final Vertex<String, String> vertex2 = DependencyGraphTest.this.graph.addVertex("v2", "p2", false);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> DependencyGraphTest.this.graph.path(vertex1, vertex2))
                    .withMessage("There is no path from v1 to v2");
        }
    }
}
