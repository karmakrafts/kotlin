package org.jetbrains.kotlin.backend.kleaver

import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
class RangedLoopTransformer : IrElementTransformerVoid() {
    override fun visitLoop(loop: IrLoop): IrExpression {
        return super.visitLoop(loop)
    }
}