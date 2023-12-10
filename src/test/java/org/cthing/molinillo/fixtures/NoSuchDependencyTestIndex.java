package org.cthing.molinillo.fixtures;

import java.util.List;
import java.util.Map;

import org.cthing.molinillo.errors.NoSuchDependencyError;


public class NoSuchDependencyTestIndex extends TestIndex {

    public NoSuchDependencyTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    public List<TestSpecification> searchFor(final TestDependency dependency) {
        throw new NoSuchDependencyError(dependency);
    }
}
