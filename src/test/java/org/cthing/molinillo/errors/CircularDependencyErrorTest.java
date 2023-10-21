package org.cthing.molinillo.errors;

import java.util.List;

import org.cthing.molinillo.graph.Vertex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CircularDependencyErrorTest {

    @Test
    public void testMessage() {
        final Vertex<String, String> vertexA = new Vertex<>("A", "payloadA");
        final Vertex<String, String> vertexB = new Vertex<>("B", "payloadB");
        final Vertex<String, String> vertexC = new Vertex<>("C", "payloadC");
        final CircularDependencyError error = new CircularDependencyError(List.of(vertexA, vertexB, vertexC));
        assertThat(error).hasMessage("There is a circular dependency between A and B and C");
    }
}
