package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.DelicateMemoryApi
import io.karma.kleaver.compiler.ExperimentalKleaverApi
import io.karma.kleaver.compiler.ForceInline
import kotlinx.cinterop.*

/**
 * @author Alexander Hinze
 * @since 24/11/2024
 */

@OptIn(ExperimentalForeignApi::class)
@ExperimentalKleaverApi
public object StackScope : DeferScope() {
    public const val DEFAULT_ALIGNMENT: Int = 16 // Works as a default on any standard OS

    @DelicateMemoryApi
    @ForceInline
    public inline fun enter(): Unit {
        nativeMemUtils.allocaEnterFrame()
    }

    @DelicateMemoryApi
    @ForceInline
    public inline fun leave(): Unit {
        executeAllDeferred()
        nativeMemUtils.allocaLeaveFrame()
    }

    @ForceInline
    public inline fun allocUnaligned(size: Int): NativePtr {
        return nativeMemUtils.alloca(size)
    }

    @ForceInline
    public inline fun alloc(size: Int, alignment: Int = DEFAULT_ALIGNMENT): NativePointed {
        val alignMask = alignment - 1
        return interpretOpaquePointed(allocUnaligned((size + alignMask) and alignMask.inv()))
    }

    @ForceInline
    public inline fun <reified T : CVariable> alloc(): T {
        return alloc(sizeOf<T>().toInt(), alignOf<T>()).reinterpret<T>()
    }

    @ForceInline
    public inline fun <reified T : CVariable> alloc(initialize: T.() -> Unit): T {
        return alloc<T>().also { it.initialize() }
    }

    @ForceInline
    public inline fun <reified T : CPointed> allocPointerTo(): CPointerVar<T> {
        return alloc<CPointerVar<T>>()
    }

    @ForceInline
    public inline fun <reified T : CVariable> allocArray(length: Int): CArrayPointer<T> {
        return alloc(sizeOf<T>().toInt() * length, alignOf<T>()).reinterpret<T>().ptr
    }

    @ForceInline
    public inline fun <reified T : CVariable> allocArray(length: Int, initialize: T.(index: Int) -> Unit): CArrayPointer<T> {
        return allocArray<T>(length).apply {
            var index = 0
            while (index < length) {
                initialize(this[index], index)
                index++
            }
        }
    }

    @ForceInline
    public inline fun <reified T : CPointed> allocArrayOfPointersTo(pointers: List<CPointer<T>>): CArrayPointer<CPointerVar<T>> {
        return allocArray<CPointerVar<T>>(pointers.size) { index ->
            value = pointers[index]
        }
    }

    @ForceInline
    public inline fun <reified T : CPointed> allocArrayOfPointersTo(vararg pointers: CPointer<T>): CArrayPointer<CPointerVar<T>> {
        return allocArray<CPointerVar<T>>(pointers.size) { index ->
            value = pointers[index]
        }
    }

    // Signed arrays

    @ForceInline
    public inline fun allocArrayOf(elements: ByteArray): CArrayPointer<ByteVar> = allocArray<ByteVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size)
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Byte): CArrayPointer<ByteVar> = allocArray<ByteVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size)
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: ShortArray): CArrayPointer<ShortVar> = allocArray<ShortVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<ShortVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Short): CArrayPointer<ShortVar> = allocArray<ShortVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<ShortVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: IntArray): CArrayPointer<IntVar> = allocArray<IntVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<IntVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Int): CArrayPointer<IntVar> = allocArray<IntVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<IntVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: LongArray): CArrayPointer<LongVar> = allocArray<LongVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<LongVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Long): CArrayPointer<LongVar> = allocArray<LongVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<LongVar>())
        }
    }

    // Unsigned arrays

    @ForceInline
    public inline fun allocArrayOf(elements: UByteArray): CArrayPointer<UByteVar> = allocArray<UByteVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size)
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UByte): CArrayPointer<UByteVar> = allocArray<UByteVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size)
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: UShortArray): CArrayPointer<UShortVar> = allocArray<UShortVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<UShortVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UShort): CArrayPointer<UShortVar> = allocArray<UShortVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<UShortVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: UIntArray): CArrayPointer<UIntVar> = allocArray<UIntVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<UIntVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: UInt): CArrayPointer<UIntVar> = allocArray<UIntVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<UIntVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: ULongArray): CArrayPointer<ULongVar> = allocArray<ULongVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<ULongVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: ULong): CArrayPointer<ULongVar> = allocArray<ULongVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<ULongVar>())
        }
    }

    // Other arrays

    @ForceInline
    public inline fun allocArrayOf(elements: FloatArray): CArrayPointer<FloatVar> = allocArray<FloatVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<FloatVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Float): CArrayPointer<FloatVar> = allocArray<FloatVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<FloatVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(elements: DoubleArray): CArrayPointer<DoubleVar> = allocArray<DoubleVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<DoubleVar>())
        }
    }

    @ForceInline
    public inline fun allocArrayOf(vararg elements: Double): CArrayPointer<DoubleVar> = allocArray<DoubleVar>(elements.size).apply {
        elements.usePinned { pinned ->
            copyMemory(rawValue, pinned.addressOf(0).rawValue, elements.size * sizeOf<DoubleVar>())
        }
    }
}

/**
 * Allows allocating CInterop variables on the native stack.
 * The parameter is explicitly declared `noinline` so we push/pop a stack frame appropriately.
 *
 * **Draft for KT-73312** Kotlin/Native: Allow proper stack memory allocation
 */
@OptIn(DelicateMemoryApi::class)
@ExperimentalKleaverApi
public inline fun <reified R> stackScoped(noinline block: StackScope.() -> R): R {
    return try {
        StackScope.enter()
        block(StackScope)
    } finally {
        StackScope.leave()
    }
}