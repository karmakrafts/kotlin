/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.MatrixType
import io.karma.kleaver.compiler.VectorType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePointed
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

    // Vector x Vector

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector2f(@VectorType(Float::class, 2) a: NativePointed, @VectorType(Float::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector2f(@VectorType(Float::class, 2) a: NativePointed, @VectorType(Float::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector2f(@VectorType(Float::class, 2) a: NativePointed, @VectorType(Float::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector2f(@VectorType(Float::class, 2) a: NativePointed, @VectorType(Float::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector2f(@VectorType(Float::class, 2) a: NativePointed, @VectorType(Float::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector2d(@VectorType(Double::class, 2) a: NativePointed, @VectorType(Double::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector2d(@VectorType(Double::class, 2) a: NativePointed, @VectorType(Double::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector2d(@VectorType(Double::class, 2) a: NativePointed, @VectorType(Double::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector2d(@VectorType(Double::class, 2) a: NativePointed, @VectorType(Double::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector2d(@VectorType(Double::class, 2) a: NativePointed, @VectorType(Double::class, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector3f(@VectorType(Float::class, 3) a: NativePointed, @VectorType(Float::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector3f(@VectorType(Float::class, 3) a: NativePointed, @VectorType(Float::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector3f(@VectorType(Float::class, 3) a: NativePointed, @VectorType(Float::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector3f(@VectorType(Float::class, 3) a: NativePointed, @VectorType(Float::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector3f(@VectorType(Float::class, 3) a: NativePointed, @VectorType(Float::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector3d(@VectorType(Double::class, 3) a: NativePointed, @VectorType(Double::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector3d(@VectorType(Double::class, 3) a: NativePointed, @VectorType(Double::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector3d(@VectorType(Double::class, 3) a: NativePointed, @VectorType(Double::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector3d(@VectorType(Double::class, 3) a: NativePointed, @VectorType(Double::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector3d(@VectorType(Double::class, 3) a: NativePointed, @VectorType(Double::class, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector4f(@VectorType(Float::class, 4) a: NativePointed, @VectorType(Float::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector4f(@VectorType(Float::class, 4) a: NativePointed, @VectorType(Float::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector4f(@VectorType(Float::class, 4) a: NativePointed, @VectorType(Float::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector4f(@VectorType(Float::class, 4) a: NativePointed, @VectorType(Float::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector4f(@VectorType(Float::class, 4) a: NativePointed, @VectorType(Float::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector4d(@VectorType(Double::class, 4) a: NativePointed, @VectorType(Double::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector4d(@VectorType(Double::class, 4) a: NativePointed, @VectorType(Double::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector4d(@VectorType(Double::class, 4) a: NativePointed, @VectorType(Double::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector4d(@VectorType(Double::class, 4) a: NativePointed, @VectorType(Double::class, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector4d(@VectorType(Double::class, 4) a: NativePointed, @VectorType(Double::class, 4) b: NativePointed)

    // Vector x Scalar

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector2f(@VectorType(Float::class, 2) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector2f(@VectorType(Float::class, 2) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector2f(@VectorType(Float::class, 2) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector2f(@VectorType(Float::class, 2) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector2f(@VectorType(Float::class, 2) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector2d(@VectorType(Double::class, 2) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector2d(@VectorType(Double::class, 2) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector2d(@VectorType(Double::class, 2) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector2d(@VectorType(Double::class, 2) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector2d(@VectorType(Double::class, 2) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector3f(@VectorType(Float::class, 3) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector3f(@VectorType(Float::class, 3) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector3f(@VectorType(Float::class, 3) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector3f(@VectorType(Float::class, 3) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector3f(@VectorType(Float::class, 3) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector3d(@VectorType(Double::class, 3) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector3d(@VectorType(Double::class, 3) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector3d(@VectorType(Double::class, 3) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector3d(@VectorType(Double::class, 3) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector3d(@VectorType(Double::class, 3) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector4f(@VectorType(Float::class, 4) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector4f(@VectorType(Float::class, 4) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector4f(@VectorType(Float::class, 4) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector4f(@VectorType(Float::class, 4) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector4f(@VectorType(Float::class, 4) a: NativePointed, b: Float)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_ADD")
    public external fun addVector4d(@VectorType(Double::class, 4) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_SUB")
    public external fun subVector4d(@VectorType(Double::class, 4) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_MUL")
    public external fun mulVector4d(@VectorType(Double::class, 4) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_DIV")
    public external fun divVector4d(@VectorType(Double::class, 4) a: NativePointed, b: Double)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_VECTOR_REM")
    public external fun remVector4d(@VectorType(Double::class, 4) a: NativePointed, b: Double)

    // Matrices

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix2x2f(@MatrixType(Float::class, 2, 2) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix3x3f(@MatrixType(Float::class, 3, 3) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix4x4f(@MatrixType(Float::class, 4, 4) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix2x2d(@MatrixType(Double::class, 2, 2) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix3x3d(@MatrixType(Double::class, 3, 3) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_TRANSPOSE")
    public external fun transposeMatrix4x4d(@MatrixType(Double::class, 4, 4) data: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix2x2f(@MatrixType(Float::class, 2, 2) a: NativePointed, @MatrixType(Float::class, 2, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix3x3f(@MatrixType(Float::class, 3, 3) a: NativePointed, @MatrixType(Float::class, 3, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix4x4f(@MatrixType(Float::class, 4, 4) a: NativePointed, @MatrixType(Float::class, 4, 4) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix2x2d(@MatrixType(Double::class, 2, 2) a: NativePointed, @MatrixType(Double::class, 2, 2) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix3x3d(@MatrixType(Double::class, 3, 3) a: NativePointed, @MatrixType(Double::class, 3, 3) b: NativePointed)

    @ExperimentalForeignApi
    @TypedIntrinsic("KLEAVER_MATRIX_MULTIPLY")
    public external fun multiplyMatrix4x4d(@MatrixType(Double::class, 4, 4) a: NativePointed, @MatrixType(Double::class, 4, 4) b: NativePointed)
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