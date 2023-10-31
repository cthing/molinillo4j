package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.SpecificationProvider;


/**
 * Error caused by conflicts between versions of a dependency.
 */
public class VersionConflictError extends ResolverError {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Conflict<?, ?>> conflicts;
    private final SpecificationProvider<?, ?> specificationProvider;

    public VersionConflictError(final Map<String, Conflict<?, ?>> conflicts,
                                final SpecificationProvider<?, ?> specificationProvider) {
        super("Unable to satisfy the following requirements:\n\n" + buildErrorMessage(conflicts));

        this.conflicts = conflicts;
        this.specificationProvider = specificationProvider;
    }

    /**
     * Obtains the conflicts that caused the resolution to fail.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @return Conflicts that cause the resolution to fail.
     */
    @SuppressWarnings("unchecked")
    public <R, S> Map<String, Conflict<R, S>> getConflicts() {
        return (Map)this.conflicts;
    }

    /**
     * Obtains the specification provider used during the resolution.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @return Specification provider used during resolution.
     */
    @SuppressWarnings("unchecked")
    public <R, S> SpecificationProvider<R, S> getSpecificationProvider() {
        return (SpecificationProvider<R, S>)this.specificationProvider;
    }

    private static String buildErrorMessage(final Map<String, Conflict<?, ?>> conflictMap) {
        final List<String> pairs = new ArrayList<>();
        conflictMap.values()
                   .forEach(conflict -> conflict.getRequirements()
                                                .forEach((source, conflictRequirements) -> conflictRequirements
                                                        .forEach(c -> pairs.add(String.format("- `%s` required by `%s`",
                                                                                              c, source.toString())))));
        return String.join("\n", pairs);
    }
}
