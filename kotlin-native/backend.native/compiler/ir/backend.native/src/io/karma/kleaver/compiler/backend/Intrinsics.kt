package io.karma.kleaver.compiler.backend

import llvm.LLVMBuildArrayAlloca
import llvm.LLVMSizeOfTypeInBits
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.llvm.FunctionGenerationContext
import org.jetbrains.kotlin.backend.konan.llvm.IntrinsicGenerator
import org.jetbrains.kotlin.backend.konan.llvm.IntrinsicType
import org.jetbrains.kotlin.backend.konan.llvm.toLLVMType
import org.jetbrains.kotlin.ir.expressions.IrCall

/**
 * @author Alexander Hinze
 * @since 23/12/2024
 */

internal fun IntrinsicGenerator.evaluateKleaverIntrinsic(
        context: FunctionGenerationContext,
        type: IntrinsicType,
        callSite: IrCall,
        args: List<LLVMValueRef>,
        resultSlot: LLVMValueRef?
): LLVMValueRef = with(context) {
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
        else -> reportNonLoweredIntrinsic(type)
    }
}

private fun FunctionGenerationContext.emitMatrixTranspose(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    TODO("")
}

private fun FunctionGenerationContext.emitMatrixMultiply(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    TODO("")
}

private fun FunctionGenerationContext.emitFma(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 4) { "Invalid number of arguments for fma intrinsic" }
    return call(
            if (callSite.symbol.owner.returnType == context.irBuiltIns.doubleType) llvm.fmuladd64Function
            else llvm.fmuladd32Function,
            args.drop(1)
    )
}

private fun FunctionGenerationContext.emitSAddSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sAddSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.saddSat8Function
        16 -> llvm.saddSat16Function
        32 -> llvm.saddSat32Function
        64 -> llvm.saddSat64Function
        else -> error("Unsupported bit width for sAddSat intrinsic")
    }, args.drop(1))
}

private fun FunctionGenerationContext.emitSSubSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sSubSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.ssubSat8Function
        16 -> llvm.ssubSat16Function
        32 -> llvm.ssubSat32Function
        64 -> llvm.ssubSat64Function
        else -> error("Unsupported bit width for sSubSat intrinsic")
    }, args.drop(1))
}

private fun FunctionGenerationContext.emitSShlSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for sShlSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.sshlSat8Function
        16 -> llvm.sshlSat16Function
        32 -> llvm.sshlSat32Function
        64 -> llvm.sshlSat64Function
        else -> error("Unsupported bit width for sShlSat intrinsic")
    }, args.drop(1))
}

private fun FunctionGenerationContext.emitUAddSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uAddSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.uaddSat8Function
        16 -> llvm.uaddSat16Function
        32 -> llvm.uaddSat32Function
        64 -> llvm.uaddSat64Function
        else -> error("Unsupported bit width for uAddSat intrinsic")
    }, args.drop(1))
}

private fun FunctionGenerationContext.emitUSubSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uSubSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.usubSat8Function
        16 -> llvm.usubSat16Function
        32 -> llvm.usubSat32Function
        64 -> llvm.usubSat64Function
        else -> error("Unsupported bit width for uSubSat intrinsic")
    }, args.drop(1))
}

private fun FunctionGenerationContext.emitUShlSat(callSite: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for uShlSat intrinsic" }
    val type = callSite.symbol.owner.returnType.toLLVMType(llvm)
    return call(when (LLVMSizeOfTypeInBits(llvmTargetData, type).toInt()) {
        8 -> llvm.ushlSat8Function
        16 -> llvm.ushlSat16Function
        32 -> llvm.ushlSat32Function
        64 -> llvm.ushlSat64Function
        else -> error("Unsupported bit width for uShlSat intrinsic")
    }, args.drop(1))
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
    return call(llvm.memcpyFunction, args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemmove(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memmove intrinsic" }
    return call(llvm.memmoveFunction, args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemset(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memset intrinsic" }
    return call(llvm.memset64Function, args + llvm.int1(false))
}

private fun FunctionGenerationContext.emitMemcmp(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 3) { "Invalid number of arguments for memcmp intrinsic" }
    return call(llvm.memcmpFunction, args)
}

private fun FunctionGenerationContext.emitStrlen(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for strlen intrinsic" }
    return call(llvm.strlenFunction, args)
}

private fun FunctionGenerationContext.emitWcslen(args: List<LLVMValueRef>): LLVMValueRef {
    require(args.size == 1) { "Invalid number of arguments for wcslen intrinsic" }
    return call(llvm.wcslenFunction, args)
}