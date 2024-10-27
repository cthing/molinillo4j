package org.cthing.molinillo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cthing.molinillo.fixtures.BundlerTestIndex;
import org.cthing.molinillo.fixtures.TestDependency;
import org.cthing.molinillo.fixtures.TestSpecification;


public class BundlerReverseTestIndex extends BundlerTestIndex {

    public BundlerReverseTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    public List<TestDependency> sortDependencies(final List<TestDependency> dependencies,
                                                 final DependencyGraph<Payload<TestDependency, TestSpecification>,
                                                         TestDependency> activated,
                                                 final Map<String, Conflict<TestDependency, TestSpecification>> conflicts) {
        final List<TestDependency> requirements = super.sortDependencies(dependencies, activated, conflicts);
        Collections.reverse(requirements);
        return requirements;
    }
}
