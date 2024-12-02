/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path

/**
 * @author Alexander Hinze
 * @since 01/12/2024
 */

internal fun transformStructTypes(state: NativeGenerationState, file: IrFile) {
    if (!LoweringAnalyzer.needsTransformation(file)) return
    KleaverLog.info { "Running StructTypeLoweringTransformer for ${file.path}" }
    // Remove all structures/unions defined in the given file
    LoweringAnalyzer.allStructTypes
            .filter { it.key == file }
            .flatMap { it.value }
            .toSet()
            .apply {
                file.declarations -= this
                if (isEmpty()) return@apply
                KleaverLog.info { "Removed $size struct types from ${file.path}" }
            }
}