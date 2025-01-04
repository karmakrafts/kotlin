/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.interop.gen.jvm

import org.jetbrains.kotlin.native.interop.gen.*
import org.jetbrains.kotlin.native.interop.indexer.*

object Plugins {
    fun plugin(pluginName: String?): Plugin = when (pluginName) {
        null -> DefaultPlugin
        else -> error("Unexpected interop plugin: $pluginName")
    }
}

interface Plugin {
    val name: String
    fun buildNativeIndex(library: NativeLibrary, verbose: Boolean, allowPrecompiledHeaders: Boolean): IndexerResult
    fun stubsBuildingContext(stubIrContext: StubIrContext): StubsBuildingContext
}

object DefaultPlugin : Plugin {
    override val name = "Default"
    override fun buildNativeIndex(library: NativeLibrary, verbose: Boolean, allowPrecompiledHeaders: Boolean): IndexerResult =
            buildNativeIndexImpl(library, verbose, allowPrecompiledHeaders)
    override fun stubsBuildingContext(stubIrContext: StubIrContext) = StubsBuildingContextImpl(stubIrContext)
}
