/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.ir.expressions.IrCall

/**
 * @author Alexander Hinze
 * @since 04/12/2024
 */
@Suppress("UNUSED", "UNUSED_VARIABLE")
internal object KleaverIntrinsics {
    fun FunctionGenerationContext.evalCall(generator: IntrinsicGenerator, callSite: IrCall, args: List<LLVMValueRef>, resultSlot: LLVMValueRef?): LLVMValueRef {
        return when (val intrinsicType = tryGetIntrinsicType(callSite)) {
            // Memory/interop functions
            IntrinsicType.KLEAVER_MEMORY_COPY -> emitMemoryCopy(generator, args)
            IntrinsicType.KLEAVER_MEMORY_SET -> emitMemorySet(generator, args)
            IntrinsicType.KLEAVER_MEMORY_MOVE -> emitMemoryMove(generator, args)
            IntrinsicType.KLEAVER_ALLOCA -> emitAlloca(args)
            IntrinsicType.KLEAVER_ADDRESS_OF -> emitAddressOf()
            IntrinsicType.KLEAVER_ALIGN_OF -> emitAlignOf()
            IntrinsicType.KLEAVER_SIZE_OF -> emitSizeOf()
            IntrinsicType.KLEAVER_OFFSET_OF -> emitOffsetOf()
            // libm intrinsics/functions
            else -> throw IllegalStateException("Unknown intrinsic type '$intrinsicType'")
        }
    }

    private fun FunctionGenerationContext.emitMemoryCopy(generator: IntrinsicGenerator, args: List<LLVMValueRef>): LLVMValueRef {
        with(generator) {
            assert(args.size == 4) { "Wrong number of arguments for memcpy intrinsic" }
            val (obj, dst, size, src) = args
            return when (size.type.sizeInBits()) {
                32 -> memcpy(dst, src, size)
                64 -> memcpy64(dst, src, size)
                else -> throw IllegalArgumentException("Unsupported size type for memcpy intrinsic")
            }
        }
    }

    private fun FunctionGenerationContext.emitMemoryMove(generator: IntrinsicGenerator, args: List<LLVMValueRef>): LLVMValueRef {
        with(generator) {
            assert(args.size == 4) { "Wrong number of arguments for memmove intrinsic" }
            val (obj, dst, size, src) = args
            return when (size.type.sizeInBits()) {
                32 -> memmove(dst, src, size)
                64 -> memmove64(dst, src, size)
                else -> throw IllegalArgumentException("Unsupported size type for memmove intrinsic")
            }
        }
    }

    private fun FunctionGenerationContext.emitMemorySet(generator: IntrinsicGenerator, args: List<LLVMValueRef>): LLVMValueRef {
        with(generator) {
            assert(args.size == 4) { "Wrong number of arguments for memset intrinsic" }
            val (obj, address, value, size) = args
            return when (size.type.sizeInBits()) {
                32 -> memset(address, value, size)
                64 -> memset64(address, value, size)
                else -> throw IllegalArgumentException("Unsupported size type for memset intrinsic")
            }
        }
    }

    private fun FunctionGenerationContext.emitAlloca(args: List<LLVMValueRef>): LLVMValueRef {
        assert(args.size == 2) { "Wrong number of arguments for alloca intrinsic" }
        return alloca(args.last())
    }

    private fun FunctionGenerationContext.emitAddressOf(): LLVMValueRef {
        TODO("Implement this")
    }

    private fun FunctionGenerationContext.emitAlignOf(): LLVMValueRef {
        TODO("Implement this")
    }

    private fun FunctionGenerationContext.emitSizeOf(): LLVMValueRef {
        TODO("Implement this")
    }

    private fun FunctionGenerationContext.emitOffsetOf(): LLVMValueRef {
        TODO("Implement this")
    }
}