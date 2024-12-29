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
import kotlin.math.min

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */

private val matrixTypeAnnotation: FqName = ClassId(KleaverNames.compilerPackage, Name.identifier("MatrixType")).asSingleFqName()

internal data class MatrixType(
        val type: IrType,
        val width: Int,
        val height: Int
) {
    fun toLLVMType(llvm: CodegenLlvmHelpers): LLVMTypeRef {
        return with(llvm) {
            matrixType(type.toLLVMType(this), width, height)
        }
    }

    fun getCommonDimension(other: MatrixType): Int = min(width, other.height)
}

internal fun IrConstructorCall.getMatrixType(): MatrixType {
    return MatrixType(
            requireNotNull(getAnnotationValueOrNull<IrType>("type")) { "Could not get matrix type" },
            requireNotNull(getAnnotationValueOrNull<Int>("width")) { "Could not get matrix width" },
            requireNotNull(getAnnotationValueOrNull<Int>("height")) { "Could not get matrix height" }
    )
}

internal fun IrValueParameter.getMatrixType(): MatrixType {
    val annotation = requireNotNull(annotations.find { it.type.classFqName == matrixTypeAnnotation }) {
        "Could not find @MatrixType annotation on parameter $name"
    }
    return annotation.getMatrixType()
}

internal fun IrSimpleFunction.getCommonMatrixType(): MatrixType {
    return parameters
            .filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
            .map { it.getMatrixType() }
            .reduce { acc, matrixType ->
                require(acc == matrixType) { "Matrix types for transpose intrinsic must match" }
                matrixType
            }
}