/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/fir/tree/tree-generator/Readme.md.
// DO NOT MODIFY IT MANUALLY.

@file:Suppress("DuplicatedCode", "unused")

package org.jetbrains.kotlin.fir.expressions.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirUnitExpression

@FirBuilderDsl
class FirUnitExpressionBuilder : FirAnnotationContainerBuilder {
    var source: KtSourceElement? = null
    override val annotations: MutableList<FirAnnotation> = mutableListOf()

    @OptIn(FirImplementationDetail::class)
    override fun build(): FirExpression {
        return FirUnitExpression(
            source,
            annotations.toMutableOrEmpty(),
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildUnitExpression(init: FirUnitExpressionBuilder.() -> Unit = {}): FirExpression {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return FirUnitExpressionBuilder().apply(init).build()
}
