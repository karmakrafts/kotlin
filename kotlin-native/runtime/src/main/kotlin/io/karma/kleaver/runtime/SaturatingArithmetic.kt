/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ForceInline
import kotlin.internal.InlineOnly
import kotlin.native.internal.IntrinsicType
import kotlin.native.internal.TypedIntrinsic

/**
 * @author Alexander Hinze
 * @since 06/12/2024
 */

// Signed

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDS)
internal external fun saturatingAddS(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDS)
internal external fun saturatingAddS(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDS)
internal external fun saturatingAddS(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDS)
internal external fun saturatingAddS(a: Long, b: Long): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBS)
internal external fun saturatingSubS(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBS)
internal external fun saturatingSubS(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBS)
internal external fun saturatingSubS(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBS)
internal external fun saturatingSubS(a: Long, b: Long): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLS)
internal external fun saturatingShlS(x: Byte, n: Int): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLS)
internal external fun saturatingShlS(x: Short, n: Int): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLS)
internal external fun saturatingShlS(x: Int, n: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLS)
internal external fun saturatingShlS(x: Long, n: Int): Long

@ForceInline
public inline infix fun Byte.satAdd(other: Byte): Byte = saturatingAddS(this, other)

@ForceInline
public inline infix fun Short.satAdd(other: Short): Short = saturatingAddS(this, other)

@ForceInline
public inline infix fun Int.satAdd(other: Int): Int = saturatingAddS(this, other)

@ForceInline
public inline infix fun Long.satAdd(other: Long): Long = saturatingAddS(this, other)

@ForceInline
public inline infix fun Byte.satSub(other: Byte): Byte = saturatingSubS(this, other)

@ForceInline
public inline infix fun Short.satSub(other: Short): Short = saturatingSubS(this, other)

@ForceInline
public inline infix fun Int.satSub(other: Int): Int = saturatingSubS(this, other)

@ForceInline
public inline infix fun Long.satSub(other: Long): Long = saturatingSubS(this, other)

@ForceInline
public inline infix fun Byte.satShl(bits: Int): Byte = saturatingShlS(this, bits)

@ForceInline
public inline infix fun Short.satShl(bits: Int): Short = saturatingShlS(this, bits)

@ForceInline
public inline infix fun Int.satShl(bits: Int): Int = saturatingShlS(this, bits)

@ForceInline
public inline infix fun Long.satShl(bits: Int): Long = saturatingShlS(this, bits)

public infix fun Byte.satMul(other: Byte): Byte {
    if (this == 0.toByte() || other == 0.toByte()) return 0.toByte()
    if (this == 1.toByte()) return other
    if (other == 1.toByte()) return this
    if (this == (-1).toByte()) return if (other == Byte.MIN_VALUE) Byte.MAX_VALUE else (-other).toByte()
    if (other == (-1).toByte()) return if (this == Byte.MIN_VALUE) Byte.MAX_VALUE else (-this).toByte()
    val mIsGthZero = this > 0.toByte()
    val mIsLthZero = this < 0.toByte()
    val fIsGthZero = other > 0.toByte()
    val fIsLthZero = other < 0.toByte()
    if (
            (mIsGthZero && fIsGthZero && this > Byte.MAX_VALUE / other)
            || (mIsLthZero && fIsLthZero && this < Byte.MAX_VALUE / other)
            || (mIsGthZero && fIsLthZero && other < Byte.MIN_VALUE / this)
            || (mIsLthZero && fIsGthZero && this < Byte.MIN_VALUE / other)
    ) return if (mIsGthZero == fIsGthZero) Byte.MAX_VALUE else Byte.MIN_VALUE
    return (this * other).toByte()
}

public infix fun Short.satMul(other: Short): Short {
    if (this == 0.toShort() || other == 0.toShort()) return 0.toShort()
    if (this == 1.toShort()) return other
    if (other == 1.toShort()) return this
    if (this == (-1).toShort()) return if (other == Short.MIN_VALUE) Short.MAX_VALUE else (-other).toShort()
    if (other == (-1).toShort()) return if (this == Short.MIN_VALUE) Short.MAX_VALUE else (-this).toShort()
    val mIsGthZero = this > 0.toShort()
    val mIsLthZero = this < 0.toShort()
    val fIsGthZero = other > 0.toShort()
    val fIsLthZero = other < 0.toShort()
    if (
            (mIsGthZero && fIsGthZero && this > Short.MAX_VALUE / other)
            || (mIsLthZero && fIsLthZero && this < Short.MAX_VALUE / other)
            || (mIsGthZero && fIsLthZero && other < Short.MIN_VALUE / this)
            || (mIsLthZero && fIsGthZero && this < Short.MIN_VALUE / other)
    ) return if (mIsGthZero == fIsGthZero) Short.MAX_VALUE else Short.MIN_VALUE
    return (this * other).toShort()
}

public infix fun Int.satMul(other: Int): Int {
    if (this == 0 || other == 0) return 0
    if (this == 1) return other
    if (other == 1) return this
    if (this == (-1)) return if (other == Int.MIN_VALUE) Int.MAX_VALUE else (-other)
    if (other == (-1)) return if (this == Int.MIN_VALUE) Int.MAX_VALUE else (-this)
    val mIsGthZero = this > 0
    val mIsLthZero = this < 0
    val fIsGthZero = other > 0
    val fIsLthZero = other < 0
    if (
            (mIsGthZero && fIsGthZero && this > Int.MAX_VALUE / other)
            || (mIsLthZero && fIsLthZero && this < Int.MAX_VALUE / other)
            || (mIsGthZero && fIsLthZero && other < Int.MIN_VALUE / this)
            || (mIsLthZero && fIsGthZero && this < Int.MIN_VALUE / other)
    ) return if (mIsGthZero == fIsGthZero) Int.MAX_VALUE else Int.MIN_VALUE
    return (this * other)
}

public infix fun Long.satMul(other: Long): Long {
    if (this == 0L || other == 0L) return 0L
    if (this == 1L) return other
    if (other == 1L) return this
    if (this == (-1L)) return if (other == Long.MIN_VALUE) Long.MAX_VALUE else (-other)
    if (other == (-1L)) return if (this == Long.MIN_VALUE) Long.MAX_VALUE else (-this)
    val mIsGthZero = this > 0L
    val mIsLthZero = this < 0L
    val fIsGthZero = other > 0L
    val fIsLthZero = other < 0L
    if (
            (mIsGthZero && fIsGthZero && this > Long.MAX_VALUE / other)
            || (mIsLthZero && fIsLthZero && this < Long.MAX_VALUE / other)
            || (mIsGthZero && fIsLthZero && other < Long.MIN_VALUE / this)
            || (mIsLthZero && fIsGthZero && this < Long.MIN_VALUE / other)
    ) return if (mIsGthZero == fIsGthZero) Long.MAX_VALUE else Long.MIN_VALUE
    return (this * other)
}

// Unsigned

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDU)
internal external fun saturatingAddU(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDU)
internal external fun saturatingAddU(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDU)
internal external fun saturatingAddU(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_ADDU)
internal external fun saturatingAddU(a: Long, b: Long): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBU)
internal external fun saturatingSubU(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBU)
internal external fun saturatingSubU(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBU)
internal external fun saturatingSubU(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SUBU)
internal external fun saturatingSubU(a: Long, b: Long): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLU)
internal external fun saturatingShlU(x: Byte, n: Int): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLU)
internal external fun saturatingShlU(x: Short, n: Int): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLU)
internal external fun saturatingShlU(x: Int, n: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SAT_SHLU)
internal external fun saturatingShlU(x: Long, n: Int): Long

@ForceInline
public inline infix fun UByte.satAdd(other: UByte): UByte = UByte(saturatingAddU(data, other.data))

@ForceInline
public inline infix fun UShort.satAdd(other: UShort): UShort = UShort(saturatingAddU(data, other.data))

@ForceInline
public inline infix fun UInt.satAdd(other: UInt): UInt = UInt(saturatingAddU(data, other.data))

@ForceInline
public inline infix fun ULong.satAdd(other: ULong): ULong = ULong(saturatingAddU(data, other.data))

@ForceInline
public inline infix fun UByte.satSub(other: UByte): UByte = UByte(saturatingSubU(data, other.data))

@ForceInline
public inline infix fun UShort.satSub(other: UShort): UShort = UShort(saturatingSubU(data, other.data))

@ForceInline
public inline infix fun UInt.satSub(other: UInt): UInt = UInt(saturatingSubU(data, other.data))

@ForceInline
public inline infix fun ULong.satSub(other: ULong): ULong = ULong(saturatingSubU(data, other.data))

@ForceInline
public inline infix fun UByte.satShl(bits: Int): UByte = UByte(saturatingShlU(data, bits))

@ForceInline
public inline infix fun UShort.satShl(bits: Int): UShort = UShort(saturatingShlU(data, bits))

@ForceInline
public inline infix fun UInt.satShl(bits: Int): UInt = UInt(saturatingShlU(data, bits))

@ForceInline
public inline infix fun ULong.satShl(bits: Int): ULong = ULong(saturatingShlU(data, bits))

@ForceInline
public inline infix fun UByte.satMul(other: UByte): UByte {
    return if (this != 0.toUByte() && other > UByte.MAX_VALUE / this) UByte.MAX_VALUE
    else (this * other).toUByte()
}

@ForceInline
public inline infix fun UShort.satMul(other: UShort): UShort {
    return if (this != 0.toUShort() && other > UShort.MAX_VALUE / this) UShort.MAX_VALUE
    else (this * other).toUShort()
}

@ForceInline
public inline infix fun UInt.satMul(other: UInt): UInt {
    return if (this != 0U && other > UInt.MAX_VALUE / this) UInt.MAX_VALUE
    else this * other
}

@ForceInline
public inline infix fun ULong.satMul(other: ULong): ULong {
    return if (this != 0UL && other > ULong.MAX_VALUE / this) ULong.MAX_VALUE
    else this * other
}