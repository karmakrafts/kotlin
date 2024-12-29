/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * @author Alexander Hinze
 * @since 29/12/2024
 */
internal object StructAnalyzer {
    private val structTypes: ArrayList<IrClass> = ArrayList()

    fun analyzeModule(module: IrModuleFragment) {
        findStructTypes(module)
    }

    private fun findStructTypes(module: IrModuleFragment) {
        module.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
        })
    }
}