/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */

/**
 * Marks the parameter to accept structures per-reference
 * instead of per-value. **May only be used on stack-allocated
 * structures as of right now.**
 */
@MustBeDocumented
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class ByRef

/**
 * Specifies the alignment of the annotated structure type in bytes.
 */
@MustBeDocumented
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class AlignAs(val alignment: Int)

/**
 * Specifies the offset of the annotated field into the
 * structures backing memory.
 */
@MustBeDocumented
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
public annotation class FieldOffset(val offset: Int)

/**
 * Specifies the bit-size of the annotated structure field
 * when the type is a signed or unsigned integer.
 */
@MustBeDocumented
@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
public annotation class BitField(val bits: Int)