package org.cthing.molinillo.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.DependencyGraph;
import org.cthing.molinillo.Payload;


public class RandomTestIndex extends TestIndex {

    public RandomTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
        final List<TestRequirement> shuffledDependencies = new ArrayList<>(dependencies);
        Collections.shuffle(shuffledDependencies);
        return shuffledDependencies;
    }
}
