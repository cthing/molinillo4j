package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.Resolution;
import org.cthing.molinillo.SpecificationProvider;


/**
 * Error caused by conflicts between versions of a dependency.
 */
@SuppressWarnings("unchecked")
public class VersionConflictError extends ResolverError {

    // Message with trees options
    public static final String SOLVER_NAME = "SolverName";
    public static final String POSSIBILITY_TYPE = "PossibilityType";
    public static final String REDUCE_TREES = "ReduceTrees";
    public static final String PRINTABLE_REQUIREMENT = "PrintableRequirement";
    public static final String ADDITIONAL_MESSAGE_FOR_CONFLICT = "AdditionalMessageForConflict";
    public static final String VERSION_FOR_SPEC = "VersionForSpec";
    public static final String INCOMPATIBLE_VERSION_MESSAGE_FOR_CONFLICT = "IncompatibleVersionMessageForConflict";
    public static final String FULL_MESSAGE_FOR_CONFLICT = "FullMessageForConflict";

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Conflict<?, ?>> conflicts;
    private final SpecificationProvider<?, ?> specificationProvider;

    public <R, S> VersionConflictError(final Map<String, Conflict<R, S>> conflicts,
                                final SpecificationProvider<R, S> specificationProvider) {
        super("Unable to satisfy the following requirements:\n\n" + buildErrorMessage(conflicts));

        this.conflicts = (Map)conflicts;
        this.specificationProvider = specificationProvider;
    }

    /**
     * Obtains the conflicts that caused the resolution to fail.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @return Conflicts that cause the resolution to fail.
     */
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
    public <R, S> SpecificationProvider<R, S> getSpecificationProvider() {
        return (SpecificationProvider<R, S>)this.specificationProvider;
    }

    /**
     * Provides a detailed explanation of the version conflict.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @param opts Options for formatting the detailed explanation
     * @return Detailed explanation of the version conflict.
     */
    public <R, S> String messageWithTrees(final Map<String, Object> opts) {
        final String solverName = (String)opts.getOrDefault(SOLVER_NAME, Resolution.class.getSimpleName());
        final String possibilityType = (String)opts.getOrDefault(POSSIBILITY_TYPE, "possibility named");
        final Function<List<List<R>>, List<List<R>>> reduceTrees =
                (Function<List<List<R>>, List<List<R>>>)opts.getOrDefault(REDUCE_TREES,
                                                                          (Function<List<List<R>>, List<List<R>>>)trees -> trees.stream().distinct().collect(Collectors.toList()));
        final Function<R, String> printableRequirement =
                (Function<R, String>)opts.getOrDefault(PRINTABLE_REQUIREMENT,
                                                       (Function<R, String>)Object::toString);
        final BiFunction<String, Conflict<R, S>, String> additionMessageForConflict =
                (BiFunction<String, Conflict<R, S>, String>)opts.getOrDefault(ADDITIONAL_MESSAGE_FOR_CONFLICT,
                                                                        (BiFunction<String, Conflict<R, S>, String>)(name, conflict) -> "");
        final Function<S, String> versionForSpec =
                (Function<S, String>)opts.getOrDefault(VERSION_FOR_SPEC, (Function<S, String>)Object::toString);
        final BiFunction<String, Conflict<R, S>, String> incompatibleVersionMessageForConflict =
                (BiFunction<String, Conflict<R, S>, String>)opts.getOrDefault(INCOMPATIBLE_VERSION_MESSAGE_FOR_CONFLICT,
                                                                        (BiFunction<String, Conflict<R, S>, String>)(name, conflict) ->
                                                                                String.format("%s could not find compatible versions for %s '%s'",
                                                                                              solverName, possibilityType, name));
        final BiFunction<String, Conflict<R, S>, String> fullMessageForConflict =
                (BiFunction<String, Conflict<R, S>, String>)opts.getOrDefault(FULL_MESSAGE_FOR_CONFLICT,
                                                                        (BiFunction<String, Conflict<R, S>, String>)(name, conflict) -> {
                    final StringBuilder buffer = new StringBuilder();
                    buffer.append('\n').append(incompatibleVersionMessageForConflict.apply(name, conflict)).append('\n');
                    if (conflict.getLockedRequirement() != null) {
                        buffer.append("  In snapshot (").append(getSpecificationProvider().nameForLockingDependencySource()).append("):\n");
                        buffer.append("    ").append(printableRequirement.apply(conflict.getLockedRequirement())).append('\n');
                        buffer.append('\n');
                    }
                    buffer.append("  In ").append(getSpecificationProvider().nameForExplicitDependencySource()).append(":\n");

                    final List<List<R>> trees = reduceTrees.apply(conflict.getRequirementTrees());
                    final String treesStr = trees.stream().map(tree -> {
                        final StringBuilder treeBuf = new StringBuilder();
                        int depth = 2;
                        for (final R req : tree) {
                            treeBuf.append("  ".repeat(depth)).append(printableRequirement.apply(req));
                            if (!tree.get(tree.size() - 1).equals(req)) {
                                final S spec = conflict.getActivatedByName().get(getSpecificationProvider().nameForDependency(req));
                                if (spec != null) {
                                    treeBuf.append(" was resolved to ").append(versionForSpec.apply(spec)).append(", which");
                                }
                                treeBuf.append(" depends on");
                            }
                            treeBuf.append('\n');
                            depth += 1;
                        }
                        return treeBuf.toString();
                    }).collect(Collectors.joining("\n"));
                    buffer.append(treesStr);

                    buffer.append(additionMessageForConflict.apply(name, conflict));

                    return buffer.toString();
        });

        return this.conflicts.entrySet()
                             .stream()
                             .sorted(Map.Entry.comparingByKey())
                             .reduce(new StringBuilder(),
                                     (o, entry) -> o.append(fullMessageForConflict.apply(entry.getKey(),
                                                                                         (Conflict<R, S>)entry.getValue())),
                                     StringBuilder::append)
                             .toString()
                             .trim();
    }

    private static <R, S> String buildErrorMessage(final Map<String, Conflict<R, S>> conflictMap) {
        final List<String> pairs = new ArrayList<>();
        conflictMap.values()
                   .forEach(conflict -> conflict.getRequirements()
                                                .forEach((source, conflictRequirements) -> conflictRequirements
                                                        .forEach(c -> pairs.add(String.format("- '%s' required by '%s'",
                                                                                              c, source.toString())))));
        return String.join("\n", pairs);
    }
}
