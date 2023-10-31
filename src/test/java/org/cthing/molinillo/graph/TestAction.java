package org.cthing.molinillo.graph;

import org.cthing.molinillo.DependencyGraph;


public class TestAction extends Action<String, String, String> {

    @Override
    public String up(final DependencyGraph<String, String> graph) {
        return "abc";
    }

    @Override
    public void down(final DependencyGraph<String, String> graph) {
    }
}
