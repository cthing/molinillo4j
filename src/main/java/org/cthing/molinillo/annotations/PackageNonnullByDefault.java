package org.cthing.molinillo.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;


/**
 * Declares that fields, method return types, method parameters, and type parameters within the annotated package
 * are not {@code null} by default. Items that can be null should be annotated with either
 * {@link javax.annotation.CheckForNull} or {@link javax.annotation.Nullable}. This annotation differs from
 * {@link javax.annotation.ParametersAreNonnullByDefault} in that in addition to method parameters, return types
 * and type parameters are also considered not {@code null} by default.
 */
@Documented
@Nonnull
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
@TypeQualifierDefault({
        ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.RECORD_COMPONENT
})
public @interface PackageNonnullByDefault {
}
