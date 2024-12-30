/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import llvm.LLVMTypeRef
import org.jetbrains.kotlin.backend.konan.llvm.CodegenLlvmHelpers
import org.jetbrains.kotlin.backend.konan.llvm.toLLVMType
import org.jetbrains.kotlin.ir.types.IrType

/**
 * @author Alexander Hinze
 * @since 30/12/2024
 */

internal data class VectorType(
        val type: IrType,
        val elementCount: Int
) {
    fun toLLVMType(llvm: CodegenLlvmHelpers): LLVMTypeRef {
        return with(llvm) {
            vectorType(type.toLLVMType(this), elementCount)
        }
    }
}