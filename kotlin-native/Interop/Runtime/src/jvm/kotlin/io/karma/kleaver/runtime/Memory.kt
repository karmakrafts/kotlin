@file:Suppress("NOTHING_TO_INLINE")

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ForceInline
import kotlinx.cinterop.Arena
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePointed
import kotlinx.cinterop.nativeMemUtils
import java.util.*

/**
 * @author Alexander Hinze
 * @since 23/12/2024
 */

@PublishedApi
internal val byteArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(ByteArray::class.java).toLong()

@PublishedApi
internal val shortArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(ShortArray::class.java).toLong()

@PublishedApi
internal val intArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(IntArray::class.java).toLong()

@PublishedApi
internal val longArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(LongArray::class.java).toLong()

@PublishedApi
internal val floatArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(FloatArray::class.java).toLong()

@PublishedApi
internal val doubleArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(DoubleArray::class.java).toLong()

@PublishedApi
internal val charArrayBaseOffset: Long = nativeMemUtils.unsafe.arrayBaseOffset(CharArray::class.java).toLong()

@PublishedApi
internal object VirtualStack {
    private val stack: Stack<Arena> = Stack()
    fun push() {
        stack.push(Arena())
    }

    fun pop() {
        stack.pop().clear()
    }

    fun peek(): Arena {
        return stack.peek()
    }
}

@PublishedApi
@ExperimentalForeignApi
@ForceInline
internal inline fun alloca(size: Int): NativePointed {
    return VirtualStack.peek().alloc(size, 1)
}

@PublishedApi
@ForceInline
internal inline fun enterStackFrame() {
    VirtualStack.push()
}

@PublishedApi
@ForceInline
internal inline fun leaveStackFrame() {
    VirtualStack.pop()
}

@PublishedApi
@ExperimentalForeignApi
@ForceInline
internal inline fun memcpy(dest: NativePointed, src: NativePointed, size: Long) {
    nativeMemUtils.unsafe.copyMemory(src.rawPtr, dest.rawPtr, size)
}

@PublishedApi
@ExperimentalForeignApi
internal fun memmove(dest: NativePointed, src: NativePointed, size: Long) {
    val unsafe = nativeMemUtils.unsafe
    val destPtr = dest.rawPtr
    val srcPtr = src.rawPtr
    if (destPtr < srcPtr) { // TODO: validate if this is correct
        var index = 0L
        while (index < size) {
            unsafe.putByte(destPtr + index, unsafe.getByte(srcPtr + index))
            ++index
        }
    } else {
        unsafe.copyMemory(src.rawPtr, dest.rawPtr, size)
    }
}

@PublishedApi
@ExperimentalForeignApi
@ForceInline
internal inline fun memset(address: NativePointed, value: Byte, size: Long) {
    nativeMemUtils.unsafe.setMemory(address.rawPtr, size, value)
}

@PublishedApi
@ExperimentalForeignApi
internal fun memcmp(addressA: NativePointed, addressB: NativePointed, size: Long): Int {
    val unsafe = nativeMemUtils.unsafe
    var index = 0L
    while (index < size) {
        val valueA = unsafe.getByte(addressA.rawPtr + index)
        val valueB = unsafe.getByte(addressB.rawPtr + index)
        when {
            valueA < valueB -> return -1
            valueA > valueB -> return 1
        }
        ++index
    }
    return 0
}

@PublishedApi
@ExperimentalForeignApi
internal fun strlen(address: NativePointed): Long {
    val unsafe = nativeMemUtils.unsafe
    var index = 0L
    while (unsafe.getByte(address.rawPtr + index) != 0.toByte()) {
        ++index
    }
    return index
}

@PublishedApi
@ExperimentalForeignApi
internal fun wcslen(address: NativePointed): Long {
    val unsafe = nativeMemUtils.unsafe
    var index = 0L
    while (unsafe.getShort(address.rawPtr + index) != 0.toShort()) {
        ++index
    }
    return index
}

// Array setters

@PublishedApi
@ExperimentalForeignApi
internal fun putByteArray(dest: ByteArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, byteArrayBaseOffset, count * Byte.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putShortArray(dest: ShortArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, shortArrayBaseOffset, count * Short.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putIntArray(dest: IntArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, intArrayBaseOffset, count * Int.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putLongArray(dest: LongArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, longArrayBaseOffset, count * Long.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUByteArray(dest: UByteArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, byteArrayBaseOffset, count * UByte.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUShortArray(dest: UShortArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, shortArrayBaseOffset, count * UShort.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUIntArray(dest: UIntArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, intArrayBaseOffset, count * UInt.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putULongArray(dest: ULongArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, longArrayBaseOffset, count * ULong.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putFloatArray(dest: FloatArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, floatArrayBaseOffset, count * Float.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putDoubleArray(dest: FloatArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, doubleArrayBaseOffset, count * Double.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun putCharArray(dest: CharArray, src: NativePointed, count: Long) {
    nativeMemUtils.unsafe.copyMemory(null, src.rawPtr, dest, charArrayBaseOffset, count * Char.SIZE_BYTES)
}

// Array getters

@PublishedApi
@ExperimentalForeignApi
internal fun getByteArray(dest: NativePointed, src: ByteArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, byteArrayBaseOffset, null, dest.rawPtr, count * Byte.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getShortArray(dest: NativePointed, src: ShortArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, shortArrayBaseOffset, null, dest.rawPtr, count * Short.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getIntArray(dest: NativePointed, src: IntArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, intArrayBaseOffset, null, dest.rawPtr, count * Int.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getLongArray(dest: NativePointed, src: LongArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, longArrayBaseOffset, null, dest.rawPtr, count * Long.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUByteArray(dest: NativePointed, src: UByteArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, byteArrayBaseOffset, null, dest.rawPtr, count * UByte.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUShortArray(dest: NativePointed, src: UShortArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, shortArrayBaseOffset, null, dest.rawPtr, count * UShort.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUIntArray(dest: NativePointed, src: UIntArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, intArrayBaseOffset, null, dest.rawPtr, count * UInt.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getULongArray(dest: NativePointed, src: ULongArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, longArrayBaseOffset, null, dest.rawPtr, count * ULong.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getFloatArray(dest: NativePointed, src: FloatArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, floatArrayBaseOffset, null, dest.rawPtr, count * Float.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getDoubleArray(dest: NativePointed, src: DoubleArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, doubleArrayBaseOffset, null, dest.rawPtr, count * Double.SIZE_BYTES)
}

@PublishedApi
@ExperimentalForeignApi
internal fun getCharArray(dest: NativePointed, src: CharArray, count: Long) {
    nativeMemUtils.unsafe.copyMemory(src, charArrayBaseOffset, null, dest.rawPtr, count * Char.SIZE_BYTES)
}