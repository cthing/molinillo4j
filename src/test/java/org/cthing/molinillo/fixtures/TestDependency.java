package org.cthing.molinillo.fixtures;

import java.util.Objects;

import org.cthing.versionparser.Version;
import org.cthing.versionparser.VersionConstraint;
import org.cthing.versionparser.VersionParsingException;
import org.cthing.versionparser.gem.GemVersionScheme;


public final class TestDependency implements Comparable<TestDependency> {

    private final String name;
    private final VersionConstraint versionConstraint;
    private final boolean prerelease;

    public TestDependency(final String name, final String... versionConstraints) {
        this.name = name;
        try {
            this.versionConstraint = GemVersionScheme.parseConstraint(versionConstraints);
        } catch (final VersionParsingException ex) {
            throw new IllegalArgumentException(ex);
        }

        if (this.versionConstraint.isAny() || this.versionConstraint.isEmpty()) {
            this.prerelease = false;
        } else {
            this.prerelease = this.versionConstraint.getRanges().stream().anyMatch(range -> {
                final Version minVersion = range.getMinVersion();
                final Version maxVersion = range.getMaxVersion();

                if (minVersion == null) {
                    return maxVersion != null && maxVersion.isPreRelease();
                }
                return minVersion.isPreRelease()
                        || (maxVersion != null && maxVersion.isPreRelease() && range.isMaxIncluded());
            });
        }
    }

    public String getName() {
        return this.name;
    }

    public VersionConstraint getVersionConstraint() {
        return this.versionConstraint;
    }

    public boolean isPreRelease() {
        return this.prerelease;
    }

    @Override
    public int compareTo(final TestDependency other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return this.name + " (" + this.versionConstraint + ')';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final TestDependency that = (TestDependency)obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.versionConstraint, that.versionConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.versionConstraint);
    }
}
