/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.runtime

import kotlin.native.internal.TypedIntrinsic

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */
public object MathIntrinsics {
    @TypedIntrinsic("KLEAVER_FMA")
    public external fun fma(a: Float, b: Float, c: Float): Float

    @TypedIntrinsic("KLEAVER_FMA")
    public external fun fma(a: Double, b: Double, c: Double): Double

    @TypedIntrinsic("KLEAVER_SADD_SAT")
    public external fun sAddSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_SSUB_SAT")
    public external fun sSubSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_SADD_SAT")
    public external fun sAddSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_SSUB_SAT")
    public external fun sSubSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_SADD_SAT")
    public external fun sAddSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_SSUB_SAT")
    public external fun sSubSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_SADD_SAT")
    public external fun sAddSat(a: Long, b: Long): Long

    @TypedIntrinsic("KLEAVER_SSUB_SAT")
    public external fun sSubSat(a: Long, b: Long): Long

    @TypedIntrinsic("KLEAVER_UADD_SAT")
    public external fun uAddSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_USUB_SAT")
    public external fun uSubSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_UADD_SAT")
    public external fun uAddSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_USUB_SAT")
    public external fun uSubSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_UADD_SAT")
    public external fun uAddSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_USUB_SAT")
    public external fun uSubSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_UADD_SAT")
    public external fun uAddSat(a: Long, b: Long): Long

    @TypedIntrinsic("KLEAVER_USUB_SAT")
    public external fun uSubSat(a: Long, b: Long): Long
}

public object BitIntrinsics {
    @TypedIntrinsic("KLEAVER_SSHL_SAT")
    public external fun sShlSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_SSHL_SAT")
    public external fun sShlSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_SSHL_SAT")
    public external fun sShlSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_SSHL_SAT")
    public external fun sShlSat(a: Long, b: Long): Long

    @TypedIntrinsic("KLEAVER_USHL_SAT")
    public external fun uShlSat(a: Byte, b: Byte): Byte

    @TypedIntrinsic("KLEAVER_USHL_SAT")
    public external fun uShlSat(a: Short, b: Short): Short

    @TypedIntrinsic("KLEAVER_USHL_SAT")
    public external fun uShlSat(a: Int, b: Int): Int

    @TypedIntrinsic("KLEAVER_USHL_SAT")
    public external fun uShlSat(a: Long, b: Long): Long
}