package org.cthing.molinillo;

import java.util.Set;

import org.cthing.molinillo.errors.ResolverError;


/**
 * Top level class for performing a dependency resolution.
 *
 * @param <R> Requirement type
 * @param <S> Specification type
 */
public class Resolver<R, S> {

    private final SpecificationProvider<R, S> specificationProvider;
    private final UI resolverUI;

    /**
     * Constructs the resolver.
     *
     * @param specificationProvider Provides information on the dependencies
     * @param resolverUI Provides output on the progress of the resolution process
     */
    public Resolver(final SpecificationProvider<R, S> specificationProvider, final UI resolverUI) {
        this.specificationProvider = specificationProvider;
        this.resolverUI = resolverUI;
    }

    /**
     * Performs the actual dependency resolution.
     *
     * @param requested Dependencies to be resolved
     * @return Graph of the resolved dependencies
     * @throws ResolverError if there is an error trying to resolve the dependencies
     */
    public DependencyGraph<S, R> resolve(final Set<R> requested)
            throws ResolverError {
        return resolve(requested, new DependencyGraph<>());
    }

    /**
     * Performs the actual dependency resolution.
     *
     * @param requested Dependencies to be resolved
     * @param base Graph of dependencies whose versions are locked
     * @return Graph of the resolved dependencies
     * @throws ResolverError if there is an error trying to resolve the dependencies
     */
    public DependencyGraph<S, R> resolve(final Set<R> requested, final DependencyGraph<R, R> base)
            throws ResolverError {
        final Resolution<R, S> resolution = new Resolution<>(this.specificationProvider, this.resolverUI,
                                                             requested, base);
        return resolution.resolve();
    }
}
