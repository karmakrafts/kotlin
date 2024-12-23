@file:Suppress("NOTHING_TO_INLINE")

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ForceInline
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.internal.TypedIntrinsic

/**
 * @author Alexander Hinze
 * @since 22/12/2024
 */

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_ALLOCA")
internal external fun alloca(size: Int): NativePointed

@PublishedApi
@ForceInline
internal inline fun enterStackFrame() {
}

@PublishedApi
@ForceInline
internal inline fun leaveStackFrame() {
}

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_MEMCPY")
internal external fun memcpy(dest: NativePointed, src: NativePointed, size: Long)

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_MEMMOVE")
internal external fun memmove(dest: NativePointed, src: NativePointed, size: Long)

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_MEMSET")
internal external fun memset(address: NativePointed, value: Byte, size: Long)

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_MEMCMP")
internal external fun memcmp(addressA: NativePointed, addressB: NativePointed, size: Long): Int

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_STRLEN")
internal external fun strlen(address: NativePointed): Long

@PublishedApi
@ExperimentalForeignApi
@TypedIntrinsic("KLEAVER_WCSLEN")
internal external fun wcslen(address: NativePointed): Long

// Arrays setters

@PublishedApi
@ExperimentalForeignApi
internal fun putByteArray(dest: ByteArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putShortArray(dest: ShortArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Short.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putIntArray(dest: IntArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Int.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putLongArray(dest: LongArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Long.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUByteArray(dest: UByteArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUShortArray(dest: UShortArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * UShort.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putUIntArray(dest: UIntArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * UInt.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putULongArray(dest: ULongArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * ULong.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putFloatArray(dest: FloatArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Float.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putDoubleArray(dest: DoubleArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Double.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun putCharArray(dest: CharArray, src: NativePointed, count: Long) {
    dest.usePinned { pinnedArray ->
        memcpy(pinnedArray.addressOf(0).pointed, src, count * Char.SIZE_BYTES)
    }
}

// Array getters

@PublishedApi
@ExperimentalForeignApi
internal fun getByteArray(dest: NativePointed, src: ByteArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getShortArray(dest: NativePointed, src: ShortArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Short.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getIntArray(dest: NativePointed, src: IntArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Int.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getLongArray(dest: NativePointed, src: LongArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Long.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUByteArray(dest: NativePointed, src: UByteArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUShortArray(dest: NativePointed, src: UShortArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * UShort.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getUIntArray(dest: NativePointed, src: UIntArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * UInt.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getULongArray(dest: NativePointed, src: ULongArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * ULong.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getFloatArray(dest: NativePointed, src: FloatArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Float.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getDoubleArray(dest: NativePointed, src: DoubleArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Double.SIZE_BYTES)
    }
}

@PublishedApi
@ExperimentalForeignApi
internal fun getCharArray(dest: NativePointed, src: CharArray, count: Long) {
    src.usePinned { pinnedArray ->
        memcpy(dest, pinnedArray.addressOf(0).pointed, count * Char.SIZE_BYTES)
    }
}