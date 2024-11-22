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

    fun setMemory(dest: NativePtr, value: Byte, size: Int): Unit =
            setMemory(interpretOpaquePointed(dest), value, size)

    fun setMemory(dest: NativePtr, value: Byte, size: Long): Unit =
            setMemory(interpretOpaquePointed(dest), value, size)

    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_COPY) external fun copyMemory(dest: NativePointed, size: Int, src: NativePointed): Unit
    @TypedIntrinsic(IntrinsicType.INTEROP_MEMORY_COPY) external fun copyMemory(dest: NativePointed, size: Long, src: NativePointed): Unit

    fun copyMemory(dest: NativePtr, size: Int, src: NativePtr): Unit =
            copyMemory(interpretOpaquePointed(dest), size, interpretOpaquePointed(src))

    fun copyMemory(dest: NativePtr, size: Long, src: NativePtr): Unit =
            copyMemory(interpretOpaquePointed(dest), size, interpretOpaquePointed(src))

    @TypedIntrinsic(IntrinsicType.INTEROP_ALLOCA) external fun alloca(size: Int): NativePtr
    fun allocaEnterFrame() {} // stubs on the native implementation
    fun allocaLeaveFrame() {} // stubs on the native implementation

    fun getByteArray(source: NativePointed, dest: ByteArray, length: Int): Unit {
        dest.usePinned { pinnedDest ->
            copyMemory(pinnedDest.addressOf(0).rawValue, length, source.reinterpret<ByteVar>().ptr.rawValue)
        }
    }

    fun putByteArray(source: ByteArray, dest: NativePointed, length: Int): Unit {
        source.usePinned { pinnedSource ->
            copyMemory(dest.reinterpret<ByteVar>().ptr.rawValue, length, pinnedSource.addressOf(0).rawValue)
        }
    }

    fun getCharArray(source: NativePointed, dest: CharArray, length: Int): Unit {
        dest.usePinned { pinnedDest ->
            copyMemory(pinnedDest.addressOf(0).rawValue, length, source.reinterpret<ShortVar>().ptr.rawValue)
        }
    }

    fun putCharArray(source: CharArray, dest: NativePointed, length: Int): Unit {
        source.usePinned { pinnedSource ->
            copyMemory(dest.reinterpret<ShortVar>().ptr.rawValue, length, pinnedSource.addressOf(0).rawValue)
        }
    }

    fun zeroMemory(dest: NativePointed, length: Int): Unit = setMemory(dest, 0, length)

    fun zeroMemory(dest: NativePointed, length: Long): Unit = setMemory(dest, 0, length)

    fun zeroMemory(dest: NativePtr, length: Int): Unit = setMemory(interpretOpaquePointed(dest), 0, length)

    fun zeroMemory(dest: NativePtr, length: Long): Unit = setMemory(interpretOpaquePointed(dest), 0, length)

    fun alloc(size: Long, align: Int): NativePointed = interpretOpaquePointed(allocRaw(size, align))

    fun free(mem: NativePtr) = freeRaw(mem)

    // Kleaver implementation end

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
