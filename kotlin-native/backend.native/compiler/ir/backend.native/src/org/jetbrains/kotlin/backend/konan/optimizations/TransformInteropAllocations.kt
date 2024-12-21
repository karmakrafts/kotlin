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
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.copyValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.findIsInstanceAnd

/**
 * A simple transformation pass after escape analysis which transforms
 * (don't want to call it lowering in this place) all applicable allocation
 * calls within a memScoped-block into a call to a stack allocation intrinsic,
 * which in turn directly emits the appropriate stack allocation instead
 * of using pre-allocated heap memory which emulates the behaviour of the stack.
 */

private val interopPackageName: FqName = FqName("kotlinx.cinterop")
private val nativePlacementName: FqName = ClassId(interopPackageName, Name.identifier("NativePlacement")).asSingleFqName()
private val arenaBaseName: FqName = ClassId(interopPackageName, Name.identifier("ArenaBase")).asSingleFqName()
private val allocFunctionName: Name = Name.identifier("alloc")
private val nativeMemUtilsName: FqName = ClassId(interopPackageName, Name.identifier("nativeMemUtils")).asSingleFqName()
private val allocaFunctionName: Name = Name.identifier("alloca")

private class InteropAllocationsTransformer(
        generationState: NativeGenerationState,
        private val lifetimes: Map<IrElement, Lifetime>
) : IrElementTransformerVoid() {
    private val allocaFunction = generationState.context.irModules.values
            .flatMap { it.files }
            .flatMap { it.declarations }
            .findIsInstanceAnd<IrSimpleFunction> {
                it.parentClassOrNull?.kotlinFqName == nativeMemUtilsName && it.name == allocaFunctionName
            } ?: error("Could not find alloca intrinsic")

    override fun visitElement(element: IrElement): IrElement {
        element.transformChildrenVoid(this)
        return element
    }

    override fun visitCall(expression: IrCall): IrExpression {
        super.visitCall(expression)
        val callee = expression.symbol.owner
        val parentName = callee.parentClassOrNull?.kotlinFqName ?: return expression
        // We need to assume that calls may be devirtualized into direct calls to ArenaBase's alloc overload
        if ((parentName != nativePlacementName && parentName != arenaBaseName) || callee.name != allocFunctionName) return expression
        val lifetime = lifetimes[expression]
        // We only care about things that never leave the scope or are exclusively used to pass down
        if (lifetime != Lifetime.LOCAL && lifetime != Lifetime.STACK && lifetime != Lifetime.ARGUMENT) return expression
        // Transform alloc -> alloca call and copy over value arguments
        return IrCallImpl(
                expression.startOffset,
                expression.endOffset,
                expression.type,
                allocaFunction.symbol
        ).apply {
            copyValueArgumentsFrom(expression, callee)
        }
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
            input.irModule.transformChildrenVoid(InteropAllocationsTransformer(generationState, input.lifetimes))
        }
)