/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol
import org.jetbrains.kotlin.fir.types.FirQualifierPart
import org.jetbrains.kotlin.name.ClassId

abstract class FirQualifierResolver : FirSessionComponent {
    abstract fun resolveSymbolWithPrefix(prefix: ClassId, remainingParts: List<FirQualifierPart>): FirClassifierSymbol<*>?
    abstract fun resolveFullyQualifiedSymbol(parts: List<FirQualifierPart>): FirClassifierSymbol<*>?
}

val FirSession.qualifierResolver: FirQualifierResolver by FirSession.sessionComponentAccessor()
