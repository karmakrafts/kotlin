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

private val NativePointed.address: Long
    get() = this.rawPtr

private enum class DataModel(val pointerSize: Long) {
    _32BIT(4),
    _64BIT(8)
}

private val dataModel: DataModel = when (System.getProperty("sun.arch.data.model")) {
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

private class VirtualStackFrame internal constructor(
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

private class VirtualStack private constructor() {
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

// Kleaver implementationn end

// Must be only used in interop, contains host pointer size, not target!
@PublishedApi
internal val pointerSize: Int = dataModel.pointerSize.toInt()

@PublishedApi
internal object nativeMemUtils {
    fun getByte(mem: NativePointed) = unsafe.getByte(mem.address)
    fun putByte(mem: NativePointed, value: Byte) = unsafe.putByte(mem.address, value)

    fun getShort(mem: NativePointed) = unsafe.getShort(mem.address)
    fun putShort(mem: NativePointed, value: Short) = unsafe.putShort(mem.address, value)
    
    fun getInt(mem: NativePointed) = unsafe.getInt(mem.address)
    fun putInt(mem: NativePointed, value: Int) = unsafe.putInt(mem.address, value)
    
    fun getLong(mem: NativePointed) = unsafe.getLong(mem.address)
    fun putLong(mem: NativePointed, value: Long) = unsafe.putLong(mem.address, value)

    fun getFloat(mem: NativePointed) = unsafe.getFloat(mem.address)
    fun putFloat(mem: NativePointed, value: Float) = unsafe.putFloat(mem.address, value)

    fun getDouble(mem: NativePointed) = unsafe.getDouble(mem.address)
    fun putDouble(mem: NativePointed, value: Double) = unsafe.putDouble(mem.address, value)

    fun getNativePtr(mem: NativePointed): NativePtr = when (dataModel) {
        DataModel._32BIT -> getInt(mem).toLong()
        DataModel._64BIT -> getLong(mem)
    }

    fun putNativePtr(mem: NativePointed, value: NativePtr) = when (dataModel) {
        DataModel._32BIT -> putInt(mem, value.toInt())
        DataModel._64BIT -> putLong(mem, value)
    }

    fun getByteArray(source: NativePointed, dest: ByteArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, byteArrayBaseOffset, length.toLong())
    }

    fun putByteArray(source: ByteArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, byteArrayBaseOffset, null, dest.address, length.toLong())
    }

    fun getCharArray(source: NativePointed, dest: CharArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, charArrayBaseOffset, length.toLong() * 2)
    }

    fun putCharArray(source: CharArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, charArrayBaseOffset, null, dest.address, length.toLong() * 2)
    }

    fun zeroMemory(dest: NativePointed, length: Int): Unit =
            unsafe.setMemory(dest.address, length.toLong(), 0)

    fun copyMemory(dest: NativePointed, length: Int, src: NativePointed) =
            unsafe.copyMemory(src.address, dest.address, length.toLong())

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T> allocateInstance(): T {
        return unsafe.allocateInstance(T::class.java) as T
    }

    // Kleaver implementation begin

    internal fun allocRaw(size: Long, align: Int): NativePtr {
        val alignMask = align.toLong() - 1
        return unsafe.allocateMemory((size + alignMask) and alignMask.inv())
    }

    internal val unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
        isAccessible = true
        return@with this.get(null) as Unsafe
    }

    fun alloca(size: Int): NativePtr {
        return VirtualStack.get().peek().alloc(size)
    }

    fun allocaEnterFrame() {
        VirtualStack.get().push()
    }

    fun allocaLeaveFrame() {
        VirtualStack.get().pop()
    }

    // Kleaver implementation end

    internal fun freeRaw(mem: NativePtr) {
        unsafe.freeMemory(mem)
    }

    fun alloc(size: Long, align: Int) = interpretOpaquePointed(nativeMemoryAllocator.alloc(size, align))

    fun free(mem: NativePtr) = nativeMemoryAllocator.free(mem)

    private val byteArrayBaseOffset = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
    private val charArrayBaseOffset = unsafe.arrayBaseOffset(CharArray::class.java).toLong()
}
