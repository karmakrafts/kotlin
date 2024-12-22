/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.phaser.KotlinBackendIrHolder
import org.jetbrains.kotlin.backend.common.phaser.createSimpleNamedCompilerPhase
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.llvm.Lifetime
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * A simple transformation pass after escape analysis which transforms
 * (don't want to call it lowering in this place) all applicable allocation
 * calls within a memScoped-block into a call to a stack allocation intrinsic,
 * which in turn directly emits the appropriate stack allocation instead
 * of using pre-allocated heap memory which emulates the behaviour of the stack.
 */

private val interopPackageName: FqName = FqName("kotlinx.cinterop")
private val allocFunctionName: Name = Name.identifier("alloc")

private val allocaFunctionSignature: IdSignature = IdSignature.CommonSignature(
        interopPackageName.asString(),
        "${interopPackageName.asString()}.alloca",
        null, 0L, null
)

internal fun isInteropAllocCallee(callee: IrSimpleFunction?): Boolean {
    if (callee == null) return false
    val signature = callee.symbol.signature ?: return false
    return signature.packageFqName() == interopPackageName && allocFunctionName.asString() in callee.name.asString()
}

private class InteropAllocationsTransformer(
        generationState: NativeGenerationState,
        private val lifetimes: Map<IrElement, Lifetime>
) : IrElementTransformerVoid() {
    private val allocaFunction: IrSimpleFunctionSymbol by lazy {
        generationState.context.symbolTable.referenceSimpleFunction(allocaFunctionSignature)
    }

    private fun computeLifetime(expression: IrExpression): Lifetime {
        return lifetimes.getOrDefault(expression, Lifetime.GLOBAL) // TODO: move default to IRRELEVANT once globally changed
    }

    private fun isApplicableCallSite(callSite: IrCall): Boolean {
        val lifetime = computeLifetime(callSite)
        return lifetime == Lifetime.LOCAL
                || lifetime == Lifetime.STACK
                || lifetime == Lifetime.ARGUMENT
    }

    override fun visitElement(element: IrElement): IrElement {
        element.transformChildrenVoid(this)
        return element
    }

    override fun visitCall(expression: IrCall): IrExpression {
        super.visitCall(expression)
        val callee = expression.symbol.owner
        if (!isApplicableCallSite(expression)) return expression
        return expression
    }
}

internal data class InteropAllocationsTransformInput(
        val irModule: IrModuleFragment,
        val lifetimes: Map<IrElement, Lifetime>
) : KotlinBackendIrHolder {
    override val kotlinIr: IrElement
        get() = irModule
}

internal val InteropAllocationsTransformPhase = createSimpleNamedCompilerPhase<NativeGenerationState, InteropAllocationsTransformInput>(
        name = "InteropAllocationsTransform",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        op = { generationState, input ->
            input.irModule.transform(InteropAllocationsTransformer(generationState, input.lifetimes), null)
        }
)