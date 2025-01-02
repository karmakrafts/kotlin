/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:OptIn(ExperimentalForeignApi::class)
package kotlinx.cinterop

import io.karma.kleaver.runtime.memcpy
import io.karma.kleaver.runtime.memset
import io.karma.kleaver.runtime.wcslen
import kotlin.native.*
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.Intrinsic
import kotlin.native.internal.TypedIntrinsic
import kotlin.native.internal.IntrinsicType

@PublishedApi
internal inline val pointerSize: Int
    get() = getPointerSize()

@PublishedApi
@TypedIntrinsic(IntrinsicType.INTEROP_GET_POINTER_SIZE)
internal external fun getPointerSize(): Int

// TODO: do not use singleton because it leads to init-check on any access.
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal object nativeMemUtils {
    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getByte(mem: NativePointed): Byte
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putByte(mem: NativePointed, value: Byte)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getShort(mem: NativePointed): Short
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putShort(mem: NativePointed, value: Short)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getInt(mem: NativePointed): Int
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putInt(mem: NativePointed, value: Int)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getLong(mem: NativePointed): Long
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putLong(mem: NativePointed, value: Long)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getFloat(mem: NativePointed): Float
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putFloat(mem: NativePointed, value: Float)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getDouble(mem: NativePointed): Double
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putDouble(mem: NativePointed, value: Double)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getNativePtr(mem: NativePointed): NativePtr
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putNativePtr(mem: NativePointed, value: NativePtr)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getVector(mem: NativePointed): Vector128
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putVector(mem: NativePointed, value: Vector128)

    inline fun getByteArray(source: NativePointed, dest: ByteArray, length: Int) {
        dest.usePinned { pinnedDest ->
            memcpy(pinnedDest.addressOf(0).pointed, source, length.toLong())
        }
    }

    inline fun putByteArray(source: ByteArray, dest: NativePointed, length: Int) {
        source.usePinned { pinnedSource ->
            memcpy(dest, pinnedSource.addressOf(0).pointed, length.toLong())
        }
    }

    inline fun getCharArray(source: NativePointed, dest: CharArray, length: Int) {
        dest.usePinned { pinnedDest ->
            memcpy(pinnedDest.addressOf(0).pointed, source, sizeOf<ShortVar>() * length.toLong())
        }
    }

    inline fun putCharArray(source: CharArray, dest: NativePointed, length: Int) {
        source.usePinned { pinnedSource ->
            memcpy(dest, pinnedSource.addressOf(0).pointed, sizeOf<ShortVar>() * length.toLong())
        }
    }

    inline fun zeroMemory(dest: NativePointed, length: Int): Unit {
        memset(dest, 0, length.toLong())
    }

    inline fun copyMemory(dest: NativePointed, length: Int, src: NativePointed): Unit {
        memcpy(dest, src, length.toLong())
    }

    fun alloc(size: Long, align: Int): NativePointed {
        return interpretOpaquePointed(allocRaw(size, align))
    }

    fun free(mem: NativePtr) {
        freeRaw(mem)
    }

    internal fun allocRaw(size: Long, align: Int): NativePtr {
        val ptr = malloc(size, align)
        if (ptr == nativeNullPtr) {
            throw OutOfMemoryError("unable to allocate native memory")
        }
        return ptr
    }

    internal fun freeRaw(mem: NativePtr) {
        cfree(mem)
    }
}

@ExperimentalForeignApi
public fun CPointer<UShortVar>.toKStringFromUtf16(): String {
    return CharArray(wcslen(pointed).toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0).pointed, pointed, sizeOf<UShortVar>() * size.toLong())
        }
    }.concatToString()
}

@ExperimentalForeignApi
public fun CPointer<ShortVar>.toKString(): String = this.toKStringFromUtf16()

@ExperimentalForeignApi
public fun CPointer<UShortVar>.toKString(): String = this.toKStringFromUtf16()

@GCUnsafeCall("Kotlin_interop_malloc")
private external fun malloc(size: Long, align: Int): NativePtr

@GCUnsafeCall("Kotlin_interop_free")
private external fun cfree(ptr: NativePtr)

@ExperimentalForeignApi
@TypedIntrinsic(IntrinsicType.INTEROP_READ_BITS)
public external fun readBits(ptr: NativePtr, offset: Long, size: Int, signed: Boolean): Long

@ExperimentalForeignApi
@TypedIntrinsic(IntrinsicType.INTEROP_WRITE_BITS)
public external fun writeBits(ptr: NativePtr, offset: Long, size: Int, value: Long)
