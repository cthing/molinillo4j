package org.cthing.molinillo.fixtures;

import java.util.Objects;

import javax.annotation.Nullable;


public class TestRequirement {

    @Nullable
    private final TestDependency dependency;

    @Nullable
    private final TestSpecification specification;

    public TestRequirement(final TestDependency dependency) {
        this.dependency = dependency;
        this.specification = null;
    }

    public TestRequirement(final TestSpecification specification) {
        this.dependency = null;
        this.specification = specification;
    }

    public TestDependency getDependency() {
        assert this.dependency != null;
        return this.dependency;
    }

    public TestSpecification getSpecification() {
        assert this.specification != null;
        return this.specification;
    }

    public boolean isDependency() {
        return this.dependency != null;
    }

    public boolean isSpecification() {
        return this.specification != null;
    }

    @SuppressWarnings("DataFlowIssue")
    public boolean isPreRelease() {
        return isDependency() ? this.dependency.isPreRelease() : this.specification.isPreRelease();
    }

    @SuppressWarnings("DataFlowIssue")
    public String getName() {
        return (this.specification == null) ? this.dependency.getName() : this.specification.getName();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public String toString() {
        return (this.specification == null) ? this.dependency.toString() : this.specification.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final TestRequirement requirement = (TestRequirement)obj;
        return Objects.equals(this.dependency, requirement.dependency)
                && Objects.equals(this.specification, requirement.specification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dependency, this.specification);
    }
}
