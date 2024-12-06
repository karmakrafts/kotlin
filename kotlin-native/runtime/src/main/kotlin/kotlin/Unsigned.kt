/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.internal.InlineOnly
import kotlin.native.internal.IntrinsicType
import kotlin.native.internal.TypedIntrinsic

// CHANGES IN THIS FILE SHOULD BE SYNCED WITH THE SAME CHANGES IN: UnsignedJVM.kt and UnsignedJs.kt
// Division and remainder are based on Guava's UnsignedLongs implementation
// Copyright 2011 The Guava Authors

// Kleaver implementation begin

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_U2F)
internal external fun _u2f(value: Int): Float

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_U2F)
internal external fun _u2f(value: Long): Double

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_U2F)
internal external fun _f2u(value: Float): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_U2F)
internal external fun _f2u(value: Double): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UDIV)
internal external fun _udiv(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UDIV)
internal external fun _udiv(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UDIV)
internal external fun _udiv(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UDIV)
internal external fun _udiv(a: Long, b: Long): Long

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UREM)
internal external fun _urem(a: Byte, b: Byte): Byte

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UREM)
internal external fun _urem(a: Short, b: Short): Short

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UREM)
internal external fun _urem(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UCMP)
internal external fun _ucmp(a: Long, b: Long): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UCMP)
internal external fun _ucmp(a: Short, b: Short): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UCMP)
internal external fun _ucmp(a: Int, b: Int): Int

@PublishedApi
@TypedIntrinsic(IntrinsicType.KLEAVER_UCMP)
internal external fun _ucmp(a: Long, b: Long): Int

// Kleaver implementation end

// Kleaver: use the intrinsic implementation of urem
@PublishedApi
internal actual fun uintRemainder(v1: UInt, v2: UInt): UInt = UInt(_urem(v1.data, v2.data))

// Kleaver: use the intrinsic implementation of udiv
@PublishedApi
internal actual fun uintDivide(v1: UInt, v2: UInt): UInt = UInt(_udiv(v1.data, v2.data))

// Kleaver: use the intrinsic implementation of udiv
@PublishedApi
internal actual fun ulongDivide(v1: ULong, v2: ULong): ULong = ULong(_udiv(v1.data, v2.data))

// Kleaver: use the intrinsic implementation of udiv
@PublishedApi
internal actual fun ulongRemainder(v1: ULong, v2: ULong): ULong = ULong(_urem(v1.data, v2.data))

// Kleaver: use the intrinsic implementation of ucmp
@PublishedApi
internal actual fun uintCompare(v1: Int, v2: Int): Int = _ucmp(v1, v2)

// Kleaver: use the intrinsic implementation of ucmp
@PublishedApi
internal actual fun ulongCompare(v1: Long, v2: Long): Int = _ucmp(v1, v2)

@PublishedApi
@InlineOnly
internal actual inline fun uintToULong(value: Int): ULong = ULong(uintToLong(value))

@PublishedApi
@InlineOnly
internal actual inline fun uintToLong(value: Int): Long = value.toLong() and 0xFFFF_FFFF

// Kleaver: use the intrinsic implementation of u2f
@PublishedApi
@InlineOnly
internal actual inline fun uintToFloat(value: Int): Float = _u2f(value)

// Kleaver: use the intrinsic implementation of f2u
@PublishedApi
@InlineOnly
internal actual inline fun floatToUInt(value: Float): UInt = _f2u(value)

@PublishedApi
internal actual fun uintToDouble(value: Int): Double = (value and Int.MAX_VALUE).toDouble() + (value ushr 31 shl 30).toDouble() * 2

@PublishedApi
internal actual fun doubleToUInt(value: Double): UInt = when {
    value.isNaN() -> 0u
    value <= UInt.MIN_VALUE.toDouble() -> UInt.MIN_VALUE
    value >= UInt.MAX_VALUE.toDouble() -> UInt.MAX_VALUE
    value <= Int.MAX_VALUE -> value.toInt().toUInt()
    else -> (value - Int.MAX_VALUE).toInt().toUInt() + Int.MAX_VALUE.toUInt()      // Int.MAX_VALUE < v < UInt.MAX_VALUE
}

@PublishedApi
@InlineOnly
internal actual inline fun ulongToFloat(value: Long): Float = ulongToDouble(value).toFloat()

@PublishedApi
@InlineOnly
internal actual inline fun floatToULong(value: Float): ULong = doubleToULong(value.toDouble())

// Kleaver: use the intrinsic implementation of f2u
@PublishedApi
internal actual fun ulongToDouble(value: Long): Double = _u2f(value)

// Kleaver: use the intrinsic implementation of f2u
@PublishedApi
internal actual fun doubleToULong(value: Double): ULong = _f2u(value)

@InlineOnly
internal actual inline fun uintToString(value: Int): String = uintToLong(value).toString()

@InlineOnly
internal actual inline fun uintToString(value: Int, base: Int): String = ulongToString(uintToLong(value), base)

@InlineOnly
internal actual inline fun ulongToString(value: Long): String = ulongToString(value, 10)

internal actual fun ulongToString(value: Long, base: Int): String {
    if (value >= 0) return value.toString(base)

    var quotient = ((value ushr 1) / base) shl 1
    var rem = value - quotient * base
    if (rem >= base) {
        rem -= base
        quotient += 1
    }
    return quotient.toString(base) + rem.toString(base)
}
