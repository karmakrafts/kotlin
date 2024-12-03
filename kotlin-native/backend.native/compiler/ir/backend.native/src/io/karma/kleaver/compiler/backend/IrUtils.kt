/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*

/**
 * @author Alexander Hinze
 * @since 01/12/2024
 */

val IrDeclaration.parentFileOrNull: IrFile?
    get() = parent.let {
        when (it) {
            is IrFile -> it
            is IrDeclaration -> it.parentFileOrNull
            else -> null
        }
    }

fun IrBuilderWithScope.irFloat(value: Float): IrConst<*> =
        IrConstImpl.float(startOffset, endOffset, context.irBuiltIns.floatType, value)

fun IrBuilderWithScope.irDouble(value: Double): IrConst<*> =
        IrConstImpl.double(startOffset, endOffset, context.irBuiltIns.floatType, value)

fun IrBuilderWithScope.createDefaultValue(
        type: IrType
): IrExpression = when {
    type.isByte() -> irByte(0)
    type.isShort() -> irShort(0)
    type.isInt() -> irInt(0)
    type.isLong() -> irLong(0)
    type.isFloat() -> irFloat(0F)
    type.isDouble() -> irDouble(0.0)
    type.isBoolean() -> irBoolean(false)
    type.isString() -> irString("")
    else -> throw IllegalStateException("No constant default value for type $this")
}