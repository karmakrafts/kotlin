/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal object KleaverLowering {
    fun lowerModules(state: NativeGenerationState, modules: List<IrModuleFragment>) {
        val types = KleaverTypes(state)
        // Transform all fields that have a union or struct type
        modules.forEachFile { transformStructFields(state, it, types) }
        // Move struct member functions to synthetic objects/classes
        modules.forEachFile { transformStructFunctions(state, it, types) }
    }
}