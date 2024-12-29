/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler

import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.reflect.KClass

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */

@ExperimentalForeignApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class MatrixType(
        val type: KClass<*>,
        val width: Int,
        val height: Int
)