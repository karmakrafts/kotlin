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
        else -> reportNonLoweredIntrinsic(type)
    }
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