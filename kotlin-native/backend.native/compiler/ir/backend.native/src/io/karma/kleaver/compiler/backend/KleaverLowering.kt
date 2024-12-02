/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */

private inline fun List<IrModuleFragment>.forEachFile(closure: (IrFile) -> Unit) {
    for (module in this) {
        for (file in module.files) {
            closure(file)
        }
    }
}

internal object KleaverLowering {
    fun lowerModules(state: NativeGenerationState, modules: List<IrModuleFragment>) {
        analyze(modules)
        transform(state, modules)
    }

    private fun analyze(modules: List<IrModuleFragment>) {
        // Find all structs
        modules.forEachFile(LoweringAnalyzer::discoverStructs)
        // Find all struct fields
        modules.forEachFile(LoweringAnalyzer::discoverStructFields)
        // Find all ByRef parameters
        modules.forEachFile(LoweringAnalyzer::discoverByRefParameters)
    }

    private fun transform(state: NativeGenerationState, modules: List<IrModuleFragment>) {
        // Transform all fields that have a union or struct type
        modules.forEachFile { transformStructFields(state, it) }
        // Transform all locals that have a union or struct type
        modules.forEachFile { transformStructLocals(state, it) }
        // Move struct member functions to a synthetic object class & transform functions
        modules.forEachFile { transformStructFunctions(state, it) }
        // Remove all residual struct classes, as they are only a compile-time construct
        modules.forEachFile { transformStructTypes(state, it) }
    }
}