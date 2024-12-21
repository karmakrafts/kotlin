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
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * A simple transformation pass after escape analysis which transforms
 * (don't want to call it lowering in this place) all applicable allocation
 * calls within a memScoped-block into a call to a stack allocation intrinsic,
 * which in turn directly emits the appropriate stack allocation instead
 * of using pre-allocated heap memory which emulates the behaviour of the stack.
 */

internal class InteropAllocationsTransformVisitor(
        private val lifetimes: Map<IrElement, Lifetime>
) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
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
        op = { _, input ->
            input.irModule.acceptVoid(InteropAllocationsTransformVisitor(input.lifetimes))
        }
)