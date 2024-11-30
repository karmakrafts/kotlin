package io.karma.kleaver.runtime

/**
 * @author Alexander Hinze
 * @since 24/11/2024
 */

@RequiresOptIn("The API you're trying to use is not part of the mainline Kotlin runtime and may break at any time")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalKleaverApi

@ExperimentalKleaverApi
@RequiresOptIn("The API you're trying to use should be used carefully as it can easily cause undefined behaviour")
@Retention(AnnotationRetention.BINARY)
public annotation class DelicateMemoryApi

/**
 * Forces the function to be inlined when already marked with the inline keyword.
 */
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
public annotation class ForceInline

/**
 * Forces the code generator to omit exception handling code in the pro- and
 * epilogue of the annotated function.
 * This can be great for performance-oriented programming when using idiomatic
 * error handling for example.
 */
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
public annotation class NeverThrows

/**
 * Prevents lambdas passed to the annotated parameters from being able to capture
 * surrounding variables. This can be used to pass closure which may be converted
 * to C-function pointers by the interop runtime.
 *
 * **Draft for KT-71839** Make @VolatileLambda public for parameters intended for native function calls
 */
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class NoCapture