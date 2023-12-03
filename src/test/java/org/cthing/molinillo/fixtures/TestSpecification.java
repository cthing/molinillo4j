package org.cthing.molinillo.fixtures;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cthing.versionparser.Version;
import org.cthing.versionparser.VersionParsingException;
import org.cthing.versionparser.gem.GemVersionScheme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public final class TestSpecification {

    private final String name;
    private final Version version;
    private final Set<TestDependency> dependencies;

    @JsonCreator
    public TestSpecification(@JsonProperty("name") final String name,
                             @JsonProperty("version") final String version,
                             @JsonProperty("dependencies") final Map<String, String> dependencies) {
        this.name = name;
        try {
            this.version = GemVersionScheme.parseVersion(version);
        } catch (final VersionParsingException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.dependencies = dependencies.entrySet().stream().map(entry -> {
            final String[] requirements = entry.getValue().split("\\s*,\\s*");
            return new TestDependency(entry.getKey(), requirements);
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getName() {
        return this.name;
    }

    public Version getVersion() {
        return this.version;
    }

    public Set<TestDependency> getDependencies() {
        return this.dependencies;
    }

    public boolean isPreRelease() {
        return this.version.isPreRelease();
    }

    @Override
    public String toString() {
        return this.name + " (" + this.version + ')';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final TestSpecification that = (TestSpecification)obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.version, that.version)
                && Objects.equals(this.dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.version, this.dependencies);
    }
}
