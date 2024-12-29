/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import llvm.LLVMIntTypeInContext
import llvm.LLVMTypeRef
import org.jetbrains.kotlin.backend.konan.llvm.CodegenLlvmHelpers
import org.jetbrains.kotlin.backend.konan.llvm.LlvmCallable
import org.jetbrains.kotlin.backend.konan.llvm.functionType

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */

@JvmInline
internal value class LazyLlvmFunction(
        private val delegate: Lazy<LlvmCallable>
) : Lazy<LlvmCallable> by delegate

internal fun CodegenLlvmHelpers.lazyIntrinsic(
        name: String,
        type: LLVMTypeRef,
        vararg attributes: String
): LazyLlvmFunction {
    return LazyLlvmFunction(lazy {
        llvmIntrinsic(name, type, *attributes)
    })
}

internal fun CodegenLlvmHelpers.importMemcpy(): LazyLlvmFunction {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memcpy.p0.p0.i64"
            else "llvm.memcpy.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemmove(): LazyLlvmFunction {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memmove.p0.p0.i64"
            else "llvm.memmove.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemset64(): LazyLlvmFunction {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memset.p0.i64"
            else "llvm.memset.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8Type, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemcmp(): LazyLlvmFunction {
    return lazyIntrinsic("memcmp", functionType(int32Type, false, int8PtrType, int8PtrType, intptrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importStrlen(): LazyLlvmFunction {
    return lazyIntrinsic("strlen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importWcslen(): LazyLlvmFunction {
    return lazyIntrinsic("wcslen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importFmuladd(bitness: Int): LazyLlvmFunction {
    val type = if (bitness == 64) doubleType else floatType
    return lazyIntrinsic("llvm.fmuladd.f$bitness", functionType(type, false, type, type, type))
}

private fun CodegenLlvmHelpers.intTypeFromWidth(width: Int): LLVMTypeRef {
    return when (width) {
        8 -> int8Type
        16 -> int16Type
        32 -> int32Type
        64 -> int64Type
        else -> requireNotNull(LLVMIntTypeInContext(llvmContext, width)) { "Could not create variable width integer type" }
    }
}

internal fun CodegenLlvmHelpers.importSAddSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.sadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSSubSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.ssub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSShlSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.sshl.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUAddSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.uadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUSubSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.usub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUShlSat(bitness: Int): LazyLlvmFunction {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.ushl.sat.i$bitness", functionType(type, false, type, type))
}