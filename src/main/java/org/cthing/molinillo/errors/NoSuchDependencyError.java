package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * An error caused by searching for a dependency that is unknown (e.g. cannot be found in a repository or has no
 * published versions).
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class NoSuchDependencyError extends ResolverError {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Object dependency;
    private final Set<Object> requiredBy;

    /**
     * Constructs the exception without specifying any dependents.
     *
     * @param dependency The dependency that could not be found
     */
    public NoSuchDependencyError(final Object dependency) {
        this(dependency, Set.of());
    }

    /**
     * Constructs the exception.
     *
     * @param dependency The dependency that could not be found
     * @param requiredBy The dependents of the dependency that could not be found
     */
    public NoSuchDependencyError(final Object dependency, final Set<Object> requiredBy) {
        this.dependency = dependency;
        this.requiredBy = requiredBy;
    }

    /**
     * Obtains the dependency that could not be found.
     *
     * @param <R> Dependency type
     * @return Dependency that could not be found.
     */
    @SuppressWarnings("unchecked")
    public <R> R getDependency() {
        return (R)this.dependency;
    }

    /**
     * Obtains the dependents of the dependency that could not be found.
     *
     * @param <R> Dependency type
     * @return Dependents of the dependency that could not be found.
     */
    @SuppressWarnings("unchecked")
    public <R> Set<R> getRequiredBy() {
        return (Set<R>)this.requiredBy;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public String getMessage() {
        return "Unable to find a specification for "
                + this.dependency
                + (this.requiredBy.isEmpty()
                   ? ""
                   : " depended upon by "
                           + this.requiredBy.stream()
                                            .map(Object::toString)
                                            .sorted()
                                            .collect(Collectors.joining(" and ")));
    }
}
