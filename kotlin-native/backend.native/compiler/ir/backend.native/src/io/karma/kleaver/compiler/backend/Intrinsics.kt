package io.karma.kleaver.compiler.backend

import llvm.LLVMBuildArrayAlloca
import llvm.LLVMSizeOfTypeInBits
import llvm.LLVMTypeRef
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.cgen.isNativePointed
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrCall

/**
 * @author Alexander Hinze
 * @since 23/12/2024
 */

internal fun IntrinsicGenerator.evaluateKleaverIntrinsic(context: FunctionGenerationContext, type: IntrinsicType, callSite: IrCall, args: List<LLVMValueRef>, resultSlot: LLVMValueRef?): LLVMValueRef = with(context) {
    when (type) {
        IntrinsicType.KLEAVER_ALLOCA -> emitAlloca(args)
        IntrinsicType.KLEAVER_MEMCPY -> emitMemcpy(args)
        IntrinsicType.KLEAVER_MEMMOVE -> emitMemmove(args)
        IntrinsicType.KLEAVER_MEMSET -> emitMemset(args)
        IntrinsicType.KLEAVER_MEMCMP -> emitMemcmp(args)
        IntrinsicType.KLEAVER_STRLEN -> emitStrlen(args)
        IntrinsicType.KLEAVER_WCSLEN -> emitWcslen(args)
        IntrinsicType.KLEAVER_SIZE_OF -> emitSizeOf(callSite, args)
        IntrinsicType.KLEAVER_ALIGN_OF -> emitAlignOf(callSite, args)
        IntrinsicType.KLEAVER_OFFSET_OF -> emitOffsetOf(callSite, args)
        IntrinsicType.KLEAVER_ADDRESS_OF -> emitAddressOf(callSite, args)
        IntrinsicType.KLEAVER_FMA -> emitFma(callSite, args)
        IntrinsicType.KLEAVER_SADD_SAT -> emitSAddSat(callSite, args)
        IntrinsicType.KLEAVER_SSUB_SAT -> emitSSubSat(callSite, args)
        IntrinsicType.KLEAVER_SSHL_SAT -> emitSShlSat(callSite, args)
        IntrinsicType.KLEAVER_UADD_SAT -> emitUAddSat(callSite, args)
        IntrinsicType.KLEAVER_USUB_SAT -> emitUSubSat(callSite, args)
        IntrinsicType.KLEAVER_USHL_SAT -> emitUShlSat(callSite, args)
        IntrinsicType.KLEAVER_MATRIX_TRANSPOSE -> emitMatrixTranspose(callSite, args)
        IntrinsicType.KLEAVER_MATRIX_MULTIPLY -> emitMatrixMultiply(callSite, args)
        IntrinsicType.KLEAVER_VECTOR_ADD -> emitVectorOp(callSite, args, ::fadd)
        IntrinsicType.KLEAVER_VECTOR_SUB -> emitVectorOp(callSite, args, ::fsub)
        IntrinsicType.KLEAVER_VECTOR_MUL -> emitVectorOp(callSite, args, ::fmul)
        IntrinsicType.KLEAVER_VECTOR_DIV -> emitVectorOp(callSite, args, ::fdiv)
        IntrinsicType.KLEAVER_VECTOR_REM -> emitVectorOp(callSite, args, ::frem)
        else -> reportNonLoweredIntrinsic(type)
    }
}

private const val SIMD_ALIGNMENT: Int = 16 // Should work on every architecture

private fun FunctionGenerationContext.emitVectorOp(callSite: IrCall, args: List<LLVMValueRef>, op: (LLVMValueRef, LLVMValueRef) -> LLVMValueRef): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for addVector intrinsic" }
    val function = callSite.symbol.owner
    val params = function.parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
    val vectorType = params[0].getVectorType()

    val type = vectorType.toLLVMType(llvm)
    val ptrType = pointerType(type)
    val address = bitcast(ptrType, args[1])
    // If the second parameter is also a pointer, assume vector, otherwise scalar
    if (params[1].type.isNativePointed(context.ir.symbols)) {
        require(params[1].getVectorType() == vectorType) { "Vector x vector intrinsics must have matching type" }
        store(op(load(type, address, alignment = SIMD_ALIGNMENT), load(type, bitcast(ptrType, args[2]), alignment = SIMD_ALIGNMENT)),
                address,
                alignment = SIMD_ALIGNMENT)
    } else {
        store(op(load(type, address, alignment = SIMD_ALIGNMENT), filledVector(type, args[2])),
                address,
                alignment = SIMD_ALIGNMENT)
    }

    return theUnitInstanceRef.llvm
}

private fun FunctionGenerationContext.emitMatrixTranspose(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 2) { "Invalid number of arguments for transposeMatrix intrinsic" }
    val function = callSite.symbol.owner
    val matrixType = function.getCommonMatrixType()
    require(matrixType.isQuadratic) { "Matrix for transpose instrinsic must be quadratic" }

    val shuffleMask = when (matrixType.width) {
        2 -> llvm.vectorI32(0, 2, 1, 3)
        3 -> llvm.vectorI32(0, 3, 6, 1, 4, 7, 2, 5, 8)
        4 -> llvm.vectorI32(0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15)
        else -> error("Unsupported matrix dimensions for transpose shuffle table")
    }

    val type = matrixType.toLLVMType(llvm)
    val address = bitcast(pointerType(type), args[1])
    store(shuffleVector(load(type, address, alignment = SIMD_ALIGNMENT), undef(type), shuffleMask), address, alignment = SIMD_ALIGNMENT)

    return theUnitInstanceRef.llvm
}

private fun FunctionGenerationContext.emitMatrixMultiply2x2(type: LLVMTypeRef, args: List<LLVMValueRef>) {
    val ptrType = pointerType(type)
    val address = bitcast(ptrType, args[1])
    val valueA = load(type, address, alignment = SIMD_ALIGNMENT)
    val valueB = load(type, bitcast(ptrType, args[2]), alignment = SIMD_ALIGNMENT)
    val row0 = shuffleVector(valueA, undef(type), llvm.vectorI32(0, 0, 1, 1))
    val row1 = shuffleVector(valueA, undef(type), llvm.vectorI32(2, 2, 3, 3))
    val col0 = shuffleVector(valueB, undef(type), llvm.vectorI32(0, 2, 0, 2))
    val col1 = shuffleVector(valueB, undef(type), llvm.vectorI32(1, 3, 1, 3))
    store(fma(type, row0, col0, fmul(row1, col1)), address, alignment = SIMD_ALIGNMENT)
}

private fun FunctionGenerationContext.emitMatrixMultiply3x3(type: LLVMTypeRef, args: List<LLVMValueRef>) {
    val ptrType = pointerType(type)
    val address = bitcast(ptrType, args[1])
    val valueA = load(type, address, alignment = SIMD_ALIGNMENT)
    val valueB = load(type, bitcast(ptrType, args[2]), alignment = SIMD_ALIGNMENT)
    val row0 = shuffleVector(valueA, undef(type), llvm.vectorI32(0, 0, 0, 1, 1, 1, 2, 2, 2))
    val row1 = shuffleVector(valueA, undef(type), llvm.vectorI32(3, 3, 3, 4, 4, 4, 5, 5, 5))
    val row2 = shuffleVector(valueA, undef(type), llvm.vectorI32(6, 6, 6, 7, 7, 7, 8, 8, 8))
    val col0 = shuffleVector(valueB, undef(type), llvm.vectorI32(0, 3, 6, 0, 3, 6, 0, 3, 6))
    val col1 = shuffleVector(valueB, undef(type), llvm.vectorI32(1, 4, 7, 1, 4, 7, 1, 4, 7))
    val col2 = shuffleVector(valueB, undef(type), llvm.vectorI32(2, 5, 8, 2, 5, 8, 2, 5, 8))
    store(fma(type, row2, col2, fma(type, row0, col0, fmul(row1, col1))), address, alignment = SIMD_ALIGNMENT)
}

private fun FunctionGenerationContext.emitMatrixMultiply4x4(type: LLVMTypeRef, args: List<LLVMValueRef>) {
    val ptrType = pointerType(type)
    val address = bitcast(ptrType, args[1])
    val valueA = load(type, address, alignment = SIMD_ALIGNMENT)
    val valueB = load(type, bitcast(ptrType, args[2]), alignment = SIMD_ALIGNMENT)
    val row0 = shuffleVector(valueA, undef(type), llvm.vectorI32(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3))
    val row1 = shuffleVector(valueA, undef(type), llvm.vectorI32(4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7))
    val row2 = shuffleVector(valueA, undef(type), llvm.vectorI32(8, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 11))
    val row3 = shuffleVector(valueA, undef(type), llvm.vectorI32(12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15, 15))
    val col0 = shuffleVector(valueB, undef(type), llvm.vectorI32(0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12))
    val col1 = shuffleVector(valueB, undef(type), llvm.vectorI32(1, 5, 9, 13, 1, 5, 9, 13, 1, 5, 9, 13, 1, 5, 9, 13))
    val col2 = shuffleVector(valueB, undef(type), llvm.vectorI32(2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14))
    val col3 = shuffleVector(valueB, undef(type), llvm.vectorI32(3, 7, 11, 15, 3, 7, 11, 15, 3, 7, 11, 15, 3, 7, 11, 15))
    store(fma(type, row3, col3, fma(type, row2, col2, fma(type, row0, col0, fmul(row1, col1)))), address, alignment = SIMD_ALIGNMENT)
}

private fun FunctionGenerationContext.emitMatrixMultiply(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for multiplyMatrix intrinsic" }
    val function = callSite.symbol.owner
    val matrixType = function.getCommonMatrixType()
    require(matrixType.isQuadratic) { "Matrix for multiply instrinsic must be quadratic" }
    val type = matrixType.toLLVMType(llvm)

    when (matrixType.width) {
        2 -> emitMatrixMultiply2x2(type, args)
        3 -> emitMatrixMultiply3x3(type, args)
        4 -> emitMatrixMultiply4x4(type, args)
    }

    return theUnitInstanceRef.llvm
}

private fun FunctionGenerationContext.emitFma(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 4) { "Invalid number of arguments for fma intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return fma(type, args.drop(1))
}

private fun FunctionGenerationContext.emitSAddSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sAddSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importSAddSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitSSubSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sSubSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importSSubSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitSShlSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sShlSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importSShlSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitUAddSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uAddSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importUAddSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitUSubSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uSubSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importUSubSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitUShlSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uShlSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(llvm.importUShlSat(LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()), args.drop(1))
}

private fun FunctionGenerationContext.emitSizeOf(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.isEmpty()) { "Invalid number of arguments for sizeOf intrinsic" }
    TODO("Implement")
}

private fun FunctionGenerationContext.emitAlignOf(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.isEmpty()) { "Invalid number of arguments for alignOf intrinsic" }
    TODO("Implement")
}

private fun FunctionGenerationContext.emitOffsetOf(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for offsetOf intrinsic" }
    TODO("Implement")
}

private fun FunctionGenerationContext.emitAddressOf(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for addressOf intrinsic" }
    TODO("Implement")
}

private fun FunctionGenerationContext.emitAlloca(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for alloca intrinsic" }
    return requireNotNull(LLVMBuildArrayAlloca(builder, llvm.int8Type, args.first(), "")) { "Could not build alloca for intrinsic call" }
}

private fun FunctionGenerationContext.emitMemcpy(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memcpy intrinsic" }
    return call(llvm.importMemcpy(), args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemmove(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memmove intrinsic" }
    return call(llvm.importMemmove(), args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemset(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memset intrinsic" }
    return call(llvm.importMemset64(), args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemcmp(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memcmp intrinsic" }
    return call(llvm.importMemcmp(), args)
}

private fun FunctionGenerationContext.emitStrlen(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for strlen intrinsic" }
    return call(llvm.importStrlen(), args)
}

private fun FunctionGenerationContext.emitWcslen(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for wcslen intrinsic" }
    return call(llvm.importWcslen(), args)
}