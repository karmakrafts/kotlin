/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * @author Alexander Hinze
 * @since 01/12/2024
 */

internal class StructFunctionLoweringTransformer(
        private val state: NativeGenerationState
) : IrElementTransformerVoid() {

}

internal fun transformStructFunctions(state: NativeGenerationState, file: IrFile) {
    if (!LoweringAnalyzer.needsTransformation(file)) return
    KleaverLog.info { "Running StructFunctionLoweringTransformer for ${file.path}" }
    file.transformChildrenVoid(StructFunctionLoweringTransformer(state))
}