/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.CodeGenerator

/**
 * @author Alexander Hinze
 * @since 30/12/2024
 */
internal fun LLVMTypeRef.getTypeName(generator: CodeGenerator): String {
    val size = LLVMSizeOfTypeInBits(generator.llvmTargetData, this)
    return when (val kind = LLVMGetTypeKind(this)) {
        LLVMTypeKind.LLVMIntegerTypeKind -> "i$size"
        LLVMTypeKind.LLVMFloatTypeKind, LLVMTypeKind.LLVMDoubleTypeKind -> "f$size"
        LLVMTypeKind.LLVMVoidTypeKind -> "void"
        LLVMTypeKind.LLVMVectorTypeKind -> {
            val dim = LLVMGetVectorSize(this)
            val elementType = requireNotNull(LLVMGetElementType(this)) { "Could not retrieve vector element type" }
            "v$dim${elementType.getTypeName(generator)}"
        }
        else -> error("Unsupported type $kind/$size")
    }
}