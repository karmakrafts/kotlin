package io.karma.kleaver.compiler

/**
 * @author Alexander Hinze
 * @since 22/12/2024
 */

/**
 * When applied to a function with the `inline` modifier,
 * this forces the inlining of the annotated function without
 * considering the regular optimization factors.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
public annotation class ForceInline