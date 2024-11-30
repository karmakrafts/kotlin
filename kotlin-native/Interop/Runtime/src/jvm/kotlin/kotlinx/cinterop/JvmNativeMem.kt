/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.cinterop

import org.jetbrains.kotlin.utils.nativeMemoryAllocator
import sun.misc.Unsafe
import java.lang.AutoCloseable
import java.lang.ThreadLocal
import java.util.Stack
import kotlin.internal.InlineOnly

// Kleaver: make this accessible as a published API
@PublishedApi
internal val NativePointed.address: Long
    get() = this.rawPtr

// Kleaver: make this accessible as a published API
@PublishedApi
internal enum class DataModel(val pointerSize: Long) {
    _32BIT(4),
    _64BIT(8)
}

// Kleaver: make this accessible as a published API
@PublishedApi
internal val dataModel: DataModel = when (System.getProperty("sun.arch.data.model")) {
    null -> TODO()
    "32" -> DataModel._32BIT
    "64" -> DataModel._64BIT
    else -> throw IllegalStateException()
}

// Kleaver implementation begin

/*
 * This simply emulates the actual stack by using temporary
 * fixed-size heap frames. Not super efficient, doesn't have to be.
 */

@PublishedApi
internal class VirtualStackFrame internal constructor(
    private val stack: VirtualStack
) : AutoCloseable {
    companion object {
        private const val SIZE: Long = 1024 * 1024 * 1024 // 1MB
    }

    private val baseAddress: NativePtr = nativeMemUtils.unsafe.allocateMemory(SIZE)
    private var offset: Long = 0

    override fun close() {
        nativeMemUtils.unsafe.freeMemory(baseAddress)
        stack.pop(this)
    }

    fun alloc(size: Int): NativePtr {
        val address = baseAddress + offset
        offset += size
        return address
    }
}

@PublishedApi
internal class VirtualStack private constructor() {
    companion object {
        private val instance: ThreadLocal<VirtualStack> = ThreadLocal.withInitial(::VirtualStack)

        fun get(): VirtualStack = instance.get()
    }

    private val frames: Stack<VirtualStackFrame> = Stack()

    fun peek(): VirtualStackFrame = frames.peek()

    fun pop(): VirtualStackFrame = frames.pop()

    fun push(): VirtualStackFrame = VirtualStackFrame(this).apply {
        frames.push(this)
    }

    internal fun pop(frame: VirtualStackFrame) {
        frames.remove(frame)
    }
}

// Must be only used in interop, contains host pointer size, not target!
@PublishedApi
internal val pointerSize: Int = dataModel.pointerSize.toInt()

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal object nativeMemUtils {
    @InlineOnly
    inline fun getByte(mem: NativePointed) = unsafe.getByte(mem.address)
    @InlineOnly
    inline fun putByte(mem: NativePointed, value: Byte) = unsafe.putByte(mem.address, value)

    @InlineOnly
    inline fun getShort(mem: NativePointed) = unsafe.getShort(mem.address)
    @InlineOnly
    inline fun putShort(mem: NativePointed, value: Short) = unsafe.putShort(mem.address, value)

    @InlineOnly
    inline fun getInt(mem: NativePointed) = unsafe.getInt(mem.address)
    @InlineOnly
    inline fun putInt(mem: NativePointed, value: Int) = unsafe.putInt(mem.address, value)

    @InlineOnly
    inline fun getLong(mem: NativePointed) = unsafe.getLong(mem.address)
    @InlineOnly
    inline fun putLong(mem: NativePointed, value: Long) = unsafe.putLong(mem.address, value)

    @InlineOnly
    inline fun getFloat(mem: NativePointed) = unsafe.getFloat(mem.address)
    @InlineOnly
    inline fun putFloat(mem: NativePointed, value: Float) = unsafe.putFloat(mem.address, value)

    @InlineOnly
    inline fun getDouble(mem: NativePointed) = unsafe.getDouble(mem.address)
    @InlineOnly
    inline fun putDouble(mem: NativePointed, value: Double) = unsafe.putDouble(mem.address, value)

    // Cannot inline these because of a bug in the WhenTable logic
    fun getNativePtr(mem: NativePointed): NativePtr = when (dataModel) {
        DataModel._32BIT -> getInt(mem).toLong()
        DataModel._64BIT -> getLong(mem)
    }

    // Cannot inline these because of a bug in the WhenTable logic
    fun putNativePtr(mem: NativePointed, value: NativePtr) = when (dataModel) {
        DataModel._32BIT -> putInt(mem, value.toInt())
        DataModel._64BIT -> putLong(mem, value)
    }

    @InlineOnly
    inline fun getByteArray(source: NativePointed, dest: ByteArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, byteArrayBaseOffset, length.toLong())
    }

    @InlineOnly
    inline fun putByteArray(source: ByteArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, byteArrayBaseOffset, null, dest.address, length.toLong())
    }

    @InlineOnly
    inline fun getCharArray(source: NativePointed, dest: CharArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, charArrayBaseOffset, length.toLong() * Char.SIZE_BYTES)
    }

    @InlineOnly
    inline fun putCharArray(source: CharArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, charArrayBaseOffset, null, dest.address, length.toLong() * Char.SIZE_BYTES)
    }

    @InlineOnly
    inline fun getFloatArray(source: NativePointed, dest: FloatArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, floatArrayBaseOffset, length.toLong() * Float.SIZE_BYTES)
    }

    @InlineOnly
    inline fun putFloatArray(source: FloatArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, floatArrayBaseOffset, null, dest.address, length.toLong() * Float.SIZE_BYTES)
    }

    @InlineOnly
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T> allocateInstance(): T {
        return unsafe.allocateInstance(T::class.java) as T
    }

    @InlineOnly
    inline fun zeroMemory(dest: NativePointed, length: Int): Unit =
            unsafe.setMemory(dest.address, length.toLong(), 0)

    @InlineOnly
    inline fun zeroMemory(dest: NativePointed, length: Long): Unit =
            unsafe.setMemory(dest.address, length, 0)

    @InlineOnly
    inline fun zeroMemory(dest: NativePtr, length: Int): Unit =
            unsafe.setMemory(dest, length.toLong(), 0)

    @InlineOnly
    inline fun zeroMemory(dest: NativePtr, length: Long): Unit =
            unsafe.setMemory(dest, length, 0)

    internal fun allocRaw(size: Long, align: Int): NativePtr {
        val alignMask = align.toLong() - 1
        return unsafe.allocateMemory((size + alignMask) and alignMask.inv())
    }

    @PublishedApi
    internal val unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
        isAccessible = true
        return@with this.get(null) as Unsafe
    }

    @InlineOnly
    inline fun alloca(size: Int): NativePtr {
        return VirtualStack.get().peek().alloc(size)
    }

    @InlineOnly
    inline fun allocaEnterFrame() {
        VirtualStack.get().push()
    }

    @InlineOnly
    inline fun allocaLeaveFrame() {
        VirtualStack.get().pop()
    }

    // copyMemory

    @InlineOnly
    inline fun copyMemory(dest: NativePointed, length: Int, src: NativePointed): Unit =
            unsafe.copyMemory(src.address, dest.address, length.toLong())

    @InlineOnly
    inline fun copyMemory(dest: NativePointed, length: Long, src: NativePointed): Unit =
            unsafe.copyMemory(src.address, dest.address, length)

    @InlineOnly
    inline fun copyMemory(dest: NativePtr, length: Int, src: NativePtr): Unit =
            unsafe.copyMemory(src, dest, length.toLong())

    @InlineOnly
    inline fun copyMemory(dest: NativePtr, length: Long, src: NativePtr): Unit =
            unsafe.copyMemory(src, dest, length)

    // setMemory

    @InlineOnly
    inline fun setMemory(dest: NativePointed, value: Byte, size: Int): Unit =
            unsafe.setMemory(dest.address, size.toLong(), value)

    @InlineOnly
    inline fun setMemory(dest: NativePointed, value: Byte, size: Long): Unit =
            unsafe.setMemory(dest.address, size, value)

    @InlineOnly
    inline fun setMemory(dest: NativePtr, value: Byte, size: Int): Unit =
            unsafe.setMemory(dest, size.toLong(), value)

    @InlineOnly
    inline fun setMemory(dest: NativePtr, value: Byte, size: Long): Unit =
            unsafe.setMemory(dest, size, value)

    // moveMemory

    fun moveMemory(dest: NativePtr, length: Int, src: NativePtr): Unit {
        if(src < dest) { // Backwards copy to allow overlap
            var index = length - 1
            while(index > 0) {
                unsafe.putByte(dest + index, unsafe.getByte(src + index))
                index++
            }
            return
        }
        unsafe.copyMemory(src, dest, length.toLong())
    }

    fun moveMemory(dest: NativePtr, length: Long, src: NativePtr): Unit {
        if(src < dest) { // Backwards copy to allow overlap
            var index = length - 1
            while(index > 0) {
                unsafe.putByte(dest + index, unsafe.getByte(src + index))
                index++
            }
            return
        }
        unsafe.copyMemory(src, dest, length)
    }

    @InlineOnly
    inline fun moveMemory(dest: NativePointed, length: Int, src: NativePointed): Unit =
            moveMemory(dest.address, length, src.address)

    @InlineOnly
    inline fun moveMemory(dest: NativePointed, length: Long, src: NativePointed): Unit =
            moveMemory(dest.address, length, src.address)

    // Kleaver implementation end

    internal fun freeRaw(mem: NativePtr) {
        unsafe.freeMemory(mem)
    }

    fun alloc(size: Long, align: Int) = interpretOpaquePointed(nativeMemoryAllocator.alloc(size, align))

    fun free(mem: NativePtr) = nativeMemoryAllocator.free(mem)

    @PublishedApi
    internal val byteArrayBaseOffset = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
    @PublishedApi
    internal val charArrayBaseOffset = unsafe.arrayBaseOffset(CharArray::class.java).toLong()
    @PublishedApi
    internal val floatArrayBaseOffset = unsafe.arrayBaseOffset(FloatArray::class.java).toLong()
}
