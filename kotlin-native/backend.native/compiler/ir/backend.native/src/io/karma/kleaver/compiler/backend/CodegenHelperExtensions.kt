/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import kotlinx.cinterop.toCValues
import llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.*

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

internal fun undef(type: LLVMTypeRef): LLVMValueRef {
    return requireNotNull(LLVMGetUndef(type)) { "Could not get undefined value for type" }
}

internal fun FunctionGenerationContext.fmul(a: LLVMValueRef, b: LLVMValueRef, name: String = ""): LLVMValueRef {
    return requireNotNull(LLVMBuildFMul(builder, a, b, name)) { "Could not create fmul instruction" }
}

internal fun FunctionGenerationContext.fdiv(a: LLVMValueRef, b: LLVMValueRef, name: String = ""): LLVMValueRef {
    return requireNotNull(LLVMBuildFDiv(builder, a, b, name)) { "Could not create fdiv instruction" }
}

internal fun FunctionGenerationContext.frem(a: LLVMValueRef, b: LLVMValueRef, name: String = ""): LLVMValueRef {
    return requireNotNull(LLVMBuildFRem(builder, a, b, name)) { "Could not create frem instruction" }
}

internal fun FunctionGenerationContext.fma(type: LLVMTypeRef, args: List<LLVMValueRef>): LLVMValueRef {
    return call(llvm.importFma(codegen, type), args)
}

internal fun FunctionGenerationContext.fma(type: LLVMTypeRef, a: LLVMValueRef, b: LLVMValueRef, c: LLVMValueRef): LLVMValueRef {
    return fma(type, listOf(a, b, c))
}

internal fun FunctionGenerationContext.shuffleVector(v1: LLVMValueRef?, v2: LLVMValueRef?, mask: LLVMValueRef?, name: String = ""): LLVMValueRef {
    return requireNotNull(LLVMBuildShuffleVector(builder, v1, v2, mask, name)) { "Could not create shufflevector instruction" }
}

internal fun FunctionGenerationContext.filledVector(type: LLVMTypeRef, initialValue: LLVMValueRef, name: String = ""): LLVMValueRef {
    val vector = LLVMBuildInsertElement(builder, undef(type), initialValue, llvm.int32(0), name)
    return shuffleVector(vector, vector, llvm.vectorI32(*IntArray(LLVMGetVectorSize(type)) { 0 }))
}

internal fun CodegenLlvmHelpers.vectorI32(vararg elements: Int): LLVMValueRef {
    return requireNotNull(LLVMConstVector(elements.map { LLVMConstInt(int32Type, it.toLong(), 0) }.toCValues(), elements.size)) { "Could not create const int vector" }
}

internal fun CodegenLlvmHelpers.vectorF32(vararg elements: Float): LLVMValueRef {
    return requireNotNull(LLVMConstVector(elements.map { LLVMConstReal(floatType, it.toDouble()) }.toCValues(), elements.size)) { "Could not create const float vector" }
}

internal fun CodegenLlvmHelpers.vectorF64(vararg elements: Double): LLVMValueRef {
    return requireNotNull(LLVMConstVector(elements.map { LLVMConstReal(doubleType, it) }.toCValues(), elements.size)) { "Could not create const double vector" }
}

internal fun CodegenLlvmHelpers.lazyIntrinsic(name: String, type: LLVMTypeRef, vararg attributes: String): LlvmCallable {
    return intrinsicCache.getOrPut(name) {
        llvmIntrinsic(name, type, *attributes)
    }
}

internal fun CodegenLlvmHelpers.importMemcpy(): LlvmCallable {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memcpy.p0.p0.i64"
            else "llvm.memcpy.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemmove(): LlvmCallable {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memmove.p0.p0.i64"
            else "llvm.memmove.p0i8.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8PtrType, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemset64(): LlvmCallable {
    return lazyIntrinsic(
            if (context.config.useLlvmOpaquePointers) "llvm.memset.p0.i64"
            else "llvm.memset.p0i8.i64",
            functionType(voidType, false, int8PtrType, int8Type, int64Type, int1Type))
}

internal fun CodegenLlvmHelpers.importMemcmp(): LlvmCallable {
    return lazyIntrinsic("memcmp", functionType(int32Type, false, int8PtrType, int8PtrType, intptrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importStrlen(): LlvmCallable {
    return lazyIntrinsic("strlen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importWcslen(): LlvmCallable {
    return lazyIntrinsic("wcslen", functionType(intptrType, false, int8PtrType), "nounwind")
}

internal fun CodegenLlvmHelpers.importFma(codegen: CodeGenerator, type: LLVMTypeRef): LlvmCallable {
    return lazyIntrinsic("llvm.fma.${type.getTypeName(codegen)}", functionType(type, false, type, type, type))
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
    return lazyIntrinsic("llvm.sadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSSubSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.ssub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importSShlSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.sshl.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUAddSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.uadd.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUSubSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.usub.sat.i$bitness", functionType(type, false, type, type))
}

internal fun CodegenLlvmHelpers.importUShlSat(bitness: Int): LlvmCallable {
    val type = intTypeFromWidth(bitness)
    return lazyIntrinsic("llvm.ushl.sat.i$bitness", functionType(type, false, type, type))
}