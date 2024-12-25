@file:Suppress("NOTHING_TO_INLINE")

package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ForceInline
import kotlinx.cinterop.*

/**
 * @author Alexander Hinze
 * @since 22/12/2024
 */
@ExperimentalForeignApi
public class StackFrame private constructor() {
    public companion object {
        @PublishedApi
        internal val instance: StackFrame = StackFrame()

        // SIMD-friendly default on 32-bit and 64-bit
        public const val DEFAULT_ALIGNMENT: Int = 16
    }

    @ForceInline
    public inline fun allocUnaligned(size: Int): NativePointed {
        val address = alloca(size)
        memset(address, 0, size.toLong())
        return address
    }

    @ForceInline
    public inline fun alloc(size: Int, alignment: Int = DEFAULT_ALIGNMENT): NativePointed {
        val address = alloca(size.align(alignment))
        memset(address, 0, size.toLong())
        return address
    }

    @ForceInline
    public inline fun <reified T : CVariable> alloc(): T {
        return alloc(sizeOf<T>().toInt(), alignOf<T>()).reinterpret()
    }

    @ForceInline
    public inline fun <reified T : CVariable> alloc(initializer: T.() -> Unit): T {
        return alloc<T>().apply(initializer)
    }

    @ForceInline
    public inline fun <reified T : CVariable> allocArray(length: Int): CArrayPointer<T> {
        return alloc(sizeOf<T>().toInt() * length, alignOf<T>()).reinterpret<T>().ptr
    }

    @ForceInline
    public inline fun <reified T : CVariable> allocArray(length: Int, initializer: T.(Int) -> Unit): CArrayPointer<T> {
        return allocArray<T>(length).also {
            for (i in 0..<length) {
                initializer(it[i], i)
            }
        }
    }

    @ForceInline
    public inline fun <T : CPointed> allocPointerTo(): CPointerVar<T> {
        return alloc<CPointerVar<T>>()
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Byte): CArrayPointer<ByteVar> {
        val data = allocArray<ByteVar>(elements.size)
        getByteArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Short): CArrayPointer<ShortVar> {
        val data = allocArray<ShortVar>(elements.size)
        getShortArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Int): CArrayPointer<IntVar> {
        val data = allocArray<IntVar>(elements.size)
        getIntArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Long): CArrayPointer<LongVar> {
        val data = allocArray<LongVar>(elements.size)
        getLongArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UByte): CArrayPointer<UByteVar> {
        val data = allocArray<UByteVar>(elements.size)
        getUByteArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UShort): CArrayPointer<UShortVar> {
        val data = allocArray<UShortVar>(elements.size)
        getUShortArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UInt): CArrayPointer<UIntVar> {
        val data = allocArray<UIntVar>(elements.size)
        getUIntArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: ULong): CArrayPointer<ULongVar> {
        val data = allocArray<ULongVar>(elements.size)
        getULongArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Float): CArrayPointer<FloatVar> {
        val data = allocArray<FloatVar>(elements.size)
        getFloatArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Double): CArrayPointer<DoubleVar> {
        val data = allocArray<DoubleVar>(elements.size)
        getDoubleArray(data.pointed, elements, elements.size.toLong())
        return data
    }

    @ForceInline
    public inline fun cstr(value: String): CArrayPointer<ByteVar> {
        val count = value.length + 1
        val data = allocArray<ByteVar>(count)
        getByteArray(data.pointed, value.encodeToByteArray(), count.toLong())
        return data
    }

    @ForceInline
    public inline fun wstr(value: String): CArrayPointer<ShortVar> {
        val count = value.length + 1
        val data = allocArray<ShortVar>(count)
        getCharArray(data.pointed, value.toCharArray(), count.toLong())
        return data
    }
}

@ExperimentalForeignApi
@ForceInline
public inline fun <reified R> stackFrame(noinline frame: StackFrame.() -> R): R {
    try {
        enterStackFrame()
        return frame(StackFrame.instance)
    } finally {
        leaveStackFrame()
    }
}