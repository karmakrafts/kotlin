@file:OptIn(ExperimentalForeignApi::class)

package io.karma.kleaver.runtime

import kotlinx.cinterop.*

/**
 * @author Alexander Hinze
 * @since 24/11/2024
 */

// copyMemory

@ForceInline
@ExperimentalKleaverApi
public inline fun copyMemory(dst: NativePtr, src: NativePtr, size: Int): Unit = nativeMemUtils.copyMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun copyMemory(dst: NativePtr, src: NativePtr, size: Long): Unit = nativeMemUtils.copyMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun copyMemory(dst: NativePointed, src: NativePointed, size: Int): Unit = nativeMemUtils.copyMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun copyMemory(dst: NativePointed, src: NativePointed, size: Long): Unit = nativeMemUtils.copyMemory(dst, size, src)

// moveMemory

@ForceInline
@ExperimentalKleaverApi
public inline fun moveMemory(dst: NativePtr, src: NativePtr, size: Int): Unit = nativeMemUtils.moveMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun moveMemory(dst: NativePtr, src: NativePtr, size: Long): Unit = nativeMemUtils.moveMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun moveMemory(dst: NativePointed, src: NativePointed, size: Int): Unit = nativeMemUtils.moveMemory(dst, size, src)

@ForceInline
@ExperimentalKleaverApi
public inline fun moveMemory(dst: NativePointed, src: NativePointed, size: Long): Unit = nativeMemUtils.moveMemory(dst, size, src)

// setMemory

@ForceInline
@ExperimentalKleaverApi
public inline fun setMemory(dst: NativePtr, size: Int, value: Byte): Unit = nativeMemUtils.setMemory(dst, value, size)

@ForceInline
@ExperimentalKleaverApi
public inline fun setMemory(dst: NativePtr, size: Long, value: Byte): Unit = nativeMemUtils.setMemory(dst, value, size)

@ForceInline
@ExperimentalKleaverApi
public inline fun setMemory(dst: NativePointed, size: Int, value: Byte): Unit = nativeMemUtils.setMemory(dst, value, size)

@ForceInline
@ExperimentalKleaverApi
public inline fun setMemory(dst: NativePointed, size: Long, value: Byte): Unit = nativeMemUtils.setMemory(dst, value, size)