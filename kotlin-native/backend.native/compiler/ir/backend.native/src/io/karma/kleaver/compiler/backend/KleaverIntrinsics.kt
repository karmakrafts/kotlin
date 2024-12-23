package io.karma.kleaver.compiler.backend

import llvm.LLVMBuildArrayAlloca
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.llvm.FunctionGenerationContext
import org.jetbrains.kotlin.backend.konan.llvm.IntrinsicGenerator
import org.jetbrains.kotlin.backend.konan.llvm.IntrinsicType
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
        else -> reportNonLoweredIntrinsic(type)
    }
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