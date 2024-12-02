/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

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

fun IrType.createDefaultValue(
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET
): IrExpression = when {
    isByte() -> IrConstImpl.byte(startOffset, endOffset, this, 0)
    isShort() -> IrConstImpl.short(startOffset, endOffset, this, 0)
    isInt() -> IrConstImpl.int(startOffset, endOffset, this, 0)
    isLong() -> IrConstImpl.long(startOffset, endOffset, this, 0)
    isFloat() -> IrConstImpl.float(startOffset, endOffset, this, 0F)
    isDouble() -> IrConstImpl.double(startOffset, endOffset, this, 0.0)
    isBoolean() -> IrConstImpl.boolean(startOffset, endOffset, this, false)
    isString() -> IrConstImpl.string(startOffset, endOffset, this, "")
    else -> throw IllegalStateException("No constant default value for type $this")
}