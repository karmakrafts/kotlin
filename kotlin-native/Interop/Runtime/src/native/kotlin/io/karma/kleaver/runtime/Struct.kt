/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.runtime

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePointed
import kotlin.native.internal.TypedIntrinsic
import kotlin.reflect.KProperty1

/**
 * @author Alexander Hinze
 * @since 28/12/2024
 */
@ExperimentalForeignApi
public interface Struct

@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_SIZE_OF")
public external fun <S : Struct> sizeOf(): Long

@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_ALIGN_OF")
public external fun <S : Struct> alignOf(): Int

@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_OFFSET_OF")
public external fun <S : Struct> offsetOf(selector: KProperty1<S, *>): Long

@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_ADDRESS_OF")
public external fun addressOf(value: Struct): NativePointed