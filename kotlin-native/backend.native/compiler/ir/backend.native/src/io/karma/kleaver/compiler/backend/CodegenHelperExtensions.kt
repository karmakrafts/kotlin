/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import llvm.LLVMIntTypeInContext
import llvm.LLVMTypeRef
import llvm.LLVMVectorType
import org.jetbrains.kotlin.backend.konan.llvm.CodegenLlvmHelpers
import org.jetbrains.kotlin.backend.konan.llvm.LlvmCallable
import org.jetbrains.kotlin.backend.konan.llvm.functionType

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */

internal fun vectorType(type: LLVMTypeRef, size: Int): LLVMTypeRef {
    return requireNotNull(LLVMVectorType(type, size)) { "Could not create vector type" }
}

internal fun matrixType(type: LLVMTypeRef, width: Int, height: Int): LLVMTypeRef {
    return vectorType(type, width * height)
}

internal fun CodegenLlvmHelpers.importMatrixTranspose(matrixType: MatrixType): LlvmCallable {
    val llvmMatrixType = matrixType.toLLVMType(this)
    return llvmIntrinsic(
            "llvm.matrix.transpose",
            functionType(llvmMatrixType, false, llvmMatrixType, int32Type, int32Type))
}

internal fun CodegenLlvmHelpers.importMatrixMultiply(matrixType: MatrixType): LlvmCallable {
    val llvmMatrixType = matrixType.toLLVMType(this)
    return llvmIntrinsic(
            "llvm.matrix.multiply",
            functionType(llvmMatrixType, false, llvmMatrixType, llvmMatrixType, int32Type, int32Type, int32Type)
    )
}

internal fun CodegenLlvmHelpers.importMemcpy(): LlvmCallable {
    return llvmIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memcpy.p0.p0.i64"
            else "llvm.memcpy.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemmove(): LlvmCallable {
    return llvmIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memmove.p0.p0.i64"
            else "llvm.memmove.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemset64(): LlvmCallable {
    return llvmIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memset.p0.i64"
            else "llvm.memset.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8Type, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemcmp(): LlvmCallable {
    return llvmIntrinsic("memcmp", functionType(int32Type, false, int8PtrType, int8PtrType, intptrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importStrlen(): LlvmCallable {
    return llvmIntrinsic("strlen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importWcslen(): LlvmCallable {
    return llvmIntrinsic("wcslen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importFmuladd(bitness: Int): LlvmCallable {
    val type = if (bitness == 64) doubleType else floatType
    return llvmIntrinsic("llvm.fmuladd.f$bitness", functionType(type, false, type, type, type))
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

internal fun CodegenLlvmHelpers.importSAddSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.sadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSSubSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.ssub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSShlSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.sshl.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUAddSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.uadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUSubSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.usub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUShlSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return llvmIntrinsic("llvm.ushl.sat.i$bitness", functionType(type, false, type, type))
}