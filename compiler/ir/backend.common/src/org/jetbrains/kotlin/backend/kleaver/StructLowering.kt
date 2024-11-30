package org.jetbrains.kotlin.backend.kleaver

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFieldAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */

class StructFieldTransformer : IrElementTransformerVoid() {
    override fun visitField(declaration: IrField): IrStatement {
        return super.visitField(declaration)
    }

    override fun visitGetField(expression: IrGetField): IrExpression {
        return super.visitGetField(expression)
    }

    override fun visitSetField(expression: IrSetField): IrExpression {
        return super.visitSetField(expression)
    }
}