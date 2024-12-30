/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import io.karma.kleaver.compiler.common.KleaverNames
import llvm.LLVMTypeRef
import org.jetbrains.kotlin.backend.konan.llvm.CodegenLlvmHelpers
import org.jetbrains.kotlin.backend.konan.llvm.toLLVMType
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.getAnnotationValueOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 30/12/2024
 */

private val vectorTypeAnnotation: FqName = ClassId(KleaverNames.compilerPackage, Name.identifier("VectorType")).asSingleFqName()

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

internal fun IrConstructorCall.getVectorType(): VectorType {
    return VectorType(
            requireNotNull(getAnnotationClassValueOrNull("type")) { "Could not get vector type" }.classType,
            requireNotNull(getAnnotationValueOrNull<Int>("elementCount")) { "Could not get vector element count" },
    )
}

internal fun IrValueParameter.getVectorType(): VectorType {
    val annotation = requireNotNull(annotations.find { it.type.classFqName == vectorTypeAnnotation }) {
        "Could not find @MatrixType annotation on parameter $name"
    }
    return annotation.getVectorType()
}

internal fun IrSimpleFunction.getCommonVectorType(): VectorType {
    return parameters
            .filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
            .map { it.getVectorType() }
            .reduce { acc, vectorType ->
                require(acc == vectorType) { "Vector types for intrinsic must match" }
                vectorType
            }
}