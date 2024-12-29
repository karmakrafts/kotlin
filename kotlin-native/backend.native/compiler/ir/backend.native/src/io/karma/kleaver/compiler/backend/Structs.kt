/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.common.phaser.createSimpleNamedCompilerPhase
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.optimizations.ModuleDFG
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */
internal data class StructPhaseInput(
    val module: IrModuleFragment,
    val dfg: ModuleDFG,
)

// Called by TopLevelPhases#runCodegen in the compiler guts
internal val StructPhase = createSimpleNamedCompilerPhase<NativeGenerationState, StructPhaseInput>(
    name = "KleaverStructs",
    preactions = getDefaultIrActions(),
    postactions = getDefaultIrActions(),
    op = { _, input ->
        StructAnalyzer.analyzeModule(input.module)
    }
)