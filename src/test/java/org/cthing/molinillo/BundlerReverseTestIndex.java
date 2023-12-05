package org.cthing.molinillo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cthing.molinillo.fixtures.BundlerTestIndex;
import org.cthing.molinillo.fixtures.TestRequirement;
import org.cthing.molinillo.fixtures.TestSpecification;


public class BundlerReverseTestIndex extends BundlerTestIndex {

    public BundlerReverseTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<TestRequirement> sortDependencies(final List<TestRequirement> dependencies,
                                                  final DependencyGraph<Payload<TestRequirement, TestSpecification>,
                                                          TestRequirement> activated,
                                                  final Map<String, Conflict<TestRequirement, TestSpecification>> conflicts) {
        final List<TestRequirement> requirements = super.sortDependencies(dependencies, activated, conflicts);
        Collections.reverse(requirements);
        return requirements;
    }
}
