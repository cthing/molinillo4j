package org.cthing.molinillo.errors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.cthing.molinillo.Conflict;
import org.cthing.molinillo.Resolution;
import org.cthing.molinillo.SpecificationProvider;


/**
 * Error caused by conflicts between versions of a dependency.
 */
@SuppressWarnings("unchecked")
public class VersionConflictError extends ResolverError {

    /**
     * Options for configuring the {@link VersionConflictError#messageWithTrees(Options)} method output.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     */
    public static class Options<R, S> {

        @Nullable
        private String solverName;

        @Nullable
        private String possibilityType;

        @Nullable
        private Function<List<List<R>>, List<List<R>>> reduceTrees;

        @Nullable
        private Function<R, String> printableRequirement;

        @Nullable
        private BiFunction<String, Conflict<R, S>, String> additionMessageForConflict;

        @Nullable
        private Function<S, String> versionForSpec;

        @Nullable
        private BiFunction<String, Conflict<R, S>, String> incompatibleVersionMessageForConflict;

        @Nullable
        private BiFunction<String, Conflict<R, S>, String> fullMessageForConflict;

        public Optional<String> getSolverName() {
            return Optional.ofNullable(this.solverName);
        }

        /**
         * Sets the name of the solver.
         *
         * @param solverName Name for the solver.
         * @return This option instance.
         */
        public Options<R, S> setSolverName(@Nullable final String solverName) {
            this.solverName = solverName;
            return this;
        }

        public Optional<String> getPossibilityType() {
            return Optional.ofNullable(this.possibilityType);
        }

        /**
         * Sets the generic name of a possibility.
         *
         * @param possibilityType Generic name for a possibility
         * @return This option instance.
         */
        public Options<R, S> setPossibilityType(@Nullable final String possibilityType) {
            this.possibilityType = possibilityType;
            return this;
        }

        public Optional<Function<List<List<R>>, List<List<R>>>> getReduceTrees() {
            return Optional.ofNullable(this.reduceTrees);
        }

        /**
         * Sets a function to reduce the requirement trees.
         *
         * @param reduceTrees Function to reduce requirement trees
         * @return This option instance.
         */
        public Options<R, S> setReduceTrees(@Nullable final Function<List<List<R>>, List<List<R>>> reduceTrees) {
            this.reduceTrees = reduceTrees;
            return this;
        }

        public Optional<Function<R, String>> getPrintableRequirement() {
            return Optional.ofNullable(this.printableRequirement);
        }

        /**
         * Sets a function to pretty-print requirements.
         *
         * @param printableRequirement Function to pretty-print requirements
         * @return This option instance.
         */
        public Options<R, S> setPrintableRequirement(@Nullable final Function<R, String> printableRequirement) {
            this.printableRequirement = printableRequirement;
            return this;
        }

        public Optional<BiFunction<String, Conflict<R, S>, String>> getAdditionMessageForConflict() {
            return Optional.ofNullable(this.additionMessageForConflict);
        }

        /**
         * Sets a function for generating additional output for the message.
         *
         * @param additionMessageForConflict Function for generating additional output
         * @return This option instance.
         */
        public Options<R, S> setAdditionMessageForConflict(
                @Nullable final BiFunction<String, Conflict<R, S>, String> additionMessageForConflict) {
            this.additionMessageForConflict = additionMessageForConflict;
            return this;
        }

        public Optional<Function<S, String>> getVersionForSpec() {
            return Optional.ofNullable(this.versionForSpec);
        }

        /**
         * Sets a function to return a version number from a possibility.
         *
         * @param versionForSpec Function to return a version number from a possibility
         * @return This option instance.
         */
        public Options<R, S> setVersionForSpec(@Nullable final Function<S, String> versionForSpec) {
            this.versionForSpec = versionForSpec;
            return this;
        }

        public Optional<BiFunction<String, Conflict<R, S>, String>> getIncompatibleVersionMessageForConflict() {
            return Optional.ofNullable(this.incompatibleVersionMessageForConflict);
        }

        /**
         * Sets a function for generating the incompatible version message.
         *
         * @param incompatibleVersionMessageForConflict Function for generating the incompatible version message
         * @return This option instance.
         */
        public Options<R, S> setIncompatibleVersionMessageForConflict(
                @Nullable final BiFunction<String, Conflict<R, S>, String> incompatibleVersionMessageForConflict) {
            this.incompatibleVersionMessageForConflict = incompatibleVersionMessageForConflict;
            return this;
        }

        public Optional<BiFunction<String, Conflict<R, S>, String>> getFullMessageForConflict() {
            return Optional.ofNullable(this.fullMessageForConflict);
        }

        /**
         * Sets a function for generating the entire version conflict message.
         *
         * @param fullMessageForConflict Function for generating the entire version conflict message
         * @return This option instance.
         */
        public Options<R, S> setFullMessageForConflict(
                @Nullable final BiFunction<String, Conflict<R, S>, String> fullMessageForConflict) {
            this.fullMessageForConflict = fullMessageForConflict;
            return this;
        }
    }


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
     * @return Detailed explanation of the version conflict using the default options.
     */
    public <R, S> String messageWithTrees() {
        return messageWithTrees(new Options<>());
    }

    /**
     * Provides a detailed explanation of the version conflict.
     *
     * @param <R> Requirement type
     * @param <S> Specification type
     * @param options Options for formatting the detailed explanation
     * @return Detailed explanation of the version conflict.
     */
    public <R, S> String messageWithTrees(final Options<R, S> options) {
        final String solverName = options.getSolverName().orElseGet(Resolution.class::getSimpleName);

        final String possibilityType = options.getPossibilityType().orElse("possibility named");

        final Function<List<List<R>>, List<List<R>>> reduceTrees =
                options.getReduceTrees().orElse(trees -> trees.stream().distinct().collect(Collectors.toList()));

        final Function<R, String> printableRequirement = options.getPrintableRequirement().orElse(Object::toString);

        final BiFunction<String, Conflict<R, S>, String> additionMessageForConflict =
                options.getAdditionMessageForConflict().orElse((name, conflict) -> "");

        final Function<S, String> versionForSpec = options.getVersionForSpec().orElse(Object::toString);

        final BiFunction<String, Conflict<R, S>, String> incompatibleVersionMessageForConflict =
                options.getIncompatibleVersionMessageForConflict()
                       .orElse((name, conflict) -> String.format("%s could not find compatible versions for %s '%s'",
                                                                 solverName, possibilityType, name));

        final BiFunction<String, Conflict<R, S>, String> fullMessageForConflict =
                options.getFullMessageForConflict().orElse((name, conflict) -> {
                    final StringBuilder buffer = new StringBuilder();
                    buffer.append('\n')
                          .append(incompatibleVersionMessageForConflict.apply(name, conflict))
                          .append('\n');
                    if (conflict.getLockedRequirement() != null) {
                        buffer.append("  In snapshot (")
                              .append(getSpecificationProvider().nameForLockingDependencySource())
                              .append("):\n")
                              .append("    ")
                              .append(printableRequirement.apply(conflict.getLockedRequirement()))
                              .append('\n')
                              .append('\n');
                    }
                    buffer.append("  In ")
                          .append(getSpecificationProvider().nameForExplicitDependencySource())
                          .append(":\n");

                    final List<List<R>> trees = reduceTrees.apply(conflict.getRequirementTrees());
                    final String treesStr = trees.stream().map(tree -> {
                        final StringBuilder treeBuf = new StringBuilder();
                        int depth = 2;
                        for (final R req : tree) {
                            treeBuf.append("  ".repeat(depth))
                                   .append(printableRequirement.apply(req));
                            if (!tree.get(tree.size() - 1).equals(req)) {
                                final S spec = conflict.getActivatedByName()
                                                       .get(getSpecificationProvider().nameForDependency(req));
                                if (spec != null) {
                                    treeBuf.append(" was resolved to ")
                                           .append(versionForSpec.apply(spec))
                                           .append(", which");
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
