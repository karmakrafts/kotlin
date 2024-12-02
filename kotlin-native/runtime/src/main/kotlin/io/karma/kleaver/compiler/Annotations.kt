/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler

/**
 * @author Alexander Hinze
 * @since 24/11/2024
 */

/**
 * Opt-in for Kleaver features which are not part of
 * the regular Kotlin/Native standard library/runtime.
 */
@MustBeDocumented
@RequiresOptIn("The API you're trying to use is not part of the mainline Kotlin runtime and may break at any time")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalKleaverApi

/**
 * Opt-in for memory APIs that shouldn't be called directly
 * but still need to be published.
 */
@MustBeDocumented
@ExperimentalKleaverApi
@RequiresOptIn("The API you're trying to use should be used carefully as it can easily cause undefined behaviour")
@Retention(AnnotationRetention.BINARY)
public annotation class DelicateMemoryApi

/**
 * Forces the function to be inlined when already marked with the inline keyword.
 */
@MustBeDocumented
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
@MustBeDocumented
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
public annotation class NeverThrows