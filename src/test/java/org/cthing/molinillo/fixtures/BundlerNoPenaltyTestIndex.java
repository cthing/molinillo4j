package org.cthing.molinillo.fixtures;

import java.util.Map;


public class BundlerNoPenaltyTestIndex extends BundlerTestIndex {

    public BundlerNoPenaltyTestIndex(final Map<String, TestSpecification[]> specsByName) {
        super(specsByName);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    protected long amountConstrained(final TestDependency dependency) {
        return 0;
    }
}
