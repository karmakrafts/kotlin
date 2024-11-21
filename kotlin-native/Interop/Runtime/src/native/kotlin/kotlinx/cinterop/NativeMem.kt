/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:OptIn(ExperimentalForeignApi::class)
package kotlinx.cinterop

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

    // Kleaver implementation begin

    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_SET) external fun setMemory(dest: NativePointed, value: Byte, size: Int): Unit
    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_SET) external fun setMemory(dest: NativePointed, value: Byte, size: Long): Unit

    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_COPY) external fun copyMemory(dest: NativePointed, size: Int, src: NativePointed): Unit
    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_COPY) external fun copyMemory(dest: NativePointed, size: Long, src: NativePointed): Unit

    @GCUnsafeCall("Kotlin_interop_alloca")
    @TypedIntrinsic(IntrinsicType.INTEROP_ALLOCA) external fun alloca(size: Int): NativePointed

    @Suppress("NOTHING_TO_INLINE")
    inline fun getByteArray(source: NativePointed, dest: ByteArray, length: Int): Unit = dest.apply {
        pin()
        copyMemory(addressOfElement(0).rawValue, length, source.reinterpret<ByteVar>().ptr.rawValue)
        unpin()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun putByteArray(source: ByteArray, dest: NativePointed, length: Int): Unit = source.apply {
        pin()
        copyMemory(source.reinterpret<ByteVar>().ptr.rawValue, length, addressOfElement(0).rawValue)
        unpin()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun getCharArray(source: NativePointed, dest: CharArray, length: Int): Unit = dest.apply {
        pin()
        copyMemory(addressOfElement(0).rawValue, length, source.reinterpret<ShortVar>().ptr.rawValue)
        unpin()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun putCharArray(source: CharArray, dest: NativePointed, length: Int): Unit = source.apply {
        pin()
        copyMemory(source.reinterpret<ShortVar>().ptr.rawValue, length, addressOfElement(0).rawValue)
        unpin()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun zeroMemory(dest: NativePointed, length: Int): Unit = setMemory(dest, 0, length)

    @Suppress("NOTHING_TO_INLINE")
    inline fun zeroMemory(dest: NativePointed, length: Long): Unit = setMemory(dest, 0, length)

    @Suppress("NOTHING_TO_INLINE")
    inline fun alloc(size: Long, align: Int): NativePointed = interpretOpaquePointed(allocRaw(size, align))

    @Suppress("NOTHING_TO_INLINE")
    inline fun free(mem: NativePtr) = freeRaw(mem)

    // Kleaver implementation end

    // TODO: bad naming, rename to mallocChecked
    internal fun allocRaw(size: Long, align: Int): NativePtr {
        val ptr = malloc(size, align)
        if (ptr == nativeNullPtr) {
            throw OutOfMemoryError("unable to allocate native memory")
        }
        return ptr
    }

    // TODO: remove this, frees are not checked anyways
    internal fun freeRaw(mem: NativePtr) {
        cfree(mem)
    }
}

// TODO: optimize this
@ExperimentalForeignApi
public fun CPointer<UShortVar>.toKStringFromUtf16(): String {
    val nativeBytes = this

    var length = 0
    while (nativeBytes[length] != 0.toUShort()) {
        ++length
    }
    val chars = kotlin.CharArray(length)
    var index = 0
    while (index < length) {
        chars[index] = nativeBytes[index].toInt().toChar()
        ++index
    }
    return chars.concatToString()
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
