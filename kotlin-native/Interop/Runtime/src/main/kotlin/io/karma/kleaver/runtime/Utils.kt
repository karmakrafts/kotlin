@file:Suppress("NOTHING_TO_INLINE")

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ForceInline
import kotlinx.cinterop.*

/**
 * @author Alexander Hinze
 * @since 23/12/2024
 */

@ExperimentalForeignApi
@ForceInline
public inline val CPointer<ByteVar>.strlen: Long
    get() = strlen(pointed)

@ExperimentalForeignApi
@ForceInline
public inline val CPointer<ShortVar>.wcslen: Long
    get() = wcslen(pointed)

@ForceInline
public inline fun Int.align(alignment: Int): Int {
    val mask = alignment - 1
    return (this + mask) and mask.inv()
}

@ForceInline
public inline fun Long.align(alignment: Int): Long {
    val mask = alignment.toLong() - 1
    return (this + mask) and mask.inv()
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.align(alignment: Int): CPointer<T>? {
    return rawValue.toLong().align(alignment).toCPointer()
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.copyToRaw(dest: CPointer<T>, size: Long) {
    memcpy(dest.pointed, pointed, size)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CVariable> CPointer<T>.copyTo(dest: CPointer<T>, count: Long) {
    memcpy(dest.pointed, pointed, sizeOf<T>() * count)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.moveToRaw(dest: CPointer<T>, size: Long) {
    memmove(dest.pointed, pointed, size)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CVariable> CPointer<T>.moveTo(dest: CPointer<T>, count: Long) {
    memmove(dest.pointed, pointed, sizeOf<T>() * count)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.fillWithRaw(value: Byte, size: Long) {
    memset(pointed, value, size)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.fillWithRaw(value: UByte, size: Long) {
    memset(pointed, value.toByte(), size)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CVariable> CPointer<T>.fillWith(value: Byte, count: Long) {
    memset(pointed, value, sizeOf<T>() * count)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CVariable> CPointer<T>.fillWith(value: UByte, count: Long) {
    memset(pointed, value.toByte(), sizeOf<T>() * count)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CPointed> CPointer<T>.comparePointedRaw(other: CPointer<T>, size: Long): Int {
    return memcmp(pointed, other.pointed, size)
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified T : CVariable> CPointer<T>.comparePointed(other: CPointer<T>, count: Long): Int {
    return memcmp(pointed, other.pointed, sizeOf<T>().toInt() * count)
}