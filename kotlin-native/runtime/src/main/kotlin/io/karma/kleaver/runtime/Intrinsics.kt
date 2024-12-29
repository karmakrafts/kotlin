/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.MatrixType
import kotlinx.cinterop.*
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

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix2x2f(@MatrixType(Float::class, 2, 2) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix2x2f(matrix: FloatArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix2x2f(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 4)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix3x3f(@MatrixType(Float::class, 3, 3) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix3x3f(matrix: FloatArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix3x3f(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 9)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix4x4f(@MatrixType(Float::class, 4, 4) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix4x4f(matrix: FloatArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix4x4f(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 16)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix2x2d(@MatrixType(Double::class, 2, 2) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix2x2d(matrix: DoubleArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix2x2d(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 4)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix3x3d(@MatrixType(Double::class, 3, 3) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix3x3d(matrix: DoubleArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix3x3d(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 9)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix4x4d(@MatrixType(Double::class, 4, 4) data: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun transposeMatrix4x4d(matrix: DoubleArray): Unit = stackFrame {
        val data = allocArrayOf(*matrix)
        transposeMatrix4x4d(data.pointed)
        matrix.usePinned { pinnedMatrix ->
            data.copyTo(pinnedMatrix.addressOf(0), 16)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix2x2f(@MatrixType(Float::class, 2, 2) a: NativePointed, @MatrixType(Float::class, 2, 2) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix2x2f(a: FloatArray, b: FloatArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix2x2f(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 4)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix3x3f(@MatrixType(Float::class, 3, 3) a: NativePointed, @MatrixType(Float::class, 3, 3) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix3x3f(a: FloatArray, b: FloatArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix3x3f(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 9)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix4x4f(@MatrixType(Float::class, 4, 4) a: NativePointed, @MatrixType(Float::class, 4, 4) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix4x4f(a: FloatArray, b: FloatArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix4x4f(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 12)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix2x2d(@MatrixType(Double::class, 2, 2) a: NativePointed, @MatrixType(Double::class, 2, 2) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix2x2d(a: DoubleArray, b: DoubleArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix2x2d(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 4)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix3x3d(@MatrixType(Double::class, 3, 3) a: NativePointed, @MatrixType(Double::class, 3, 3) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix3x3d(a: DoubleArray, b: DoubleArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix3x3d(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 9)
        }
    }

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix4x4d(@MatrixType(Double::class, 4, 4) a: NativePointed, @MatrixType(Double::class, 4, 4) b: NativePointed)

    @OptIn(ExperimentalForeignApi::class)
    public fun multiplyMatrix4x4d(a: DoubleArray, b: DoubleArray): Unit = stackFrame {
        val dataA = allocArrayOf(*a)
        val dataB = allocArrayOf(*b)
        multiplyMatrix4x4d(dataA.pointed, dataB.pointed)
        a.usePinned { pinnedA ->
            dataA.copyTo(pinnedA.addressOf(0), 16)
        }
    }
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