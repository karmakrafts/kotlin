/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.ir.implementedInterfaces
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal object LoweringAnalyzer {
    private val structTypes: HashMap<IrDeclarationContainer, ArrayList<IrClass>> = HashMap()
    private val unionTypes: HashMap<IrDeclarationContainer, ArrayList<IrClass>> = HashMap()
    private val structFields: HashMap<IrDeclarationContainer, ArrayList<IrProperty>> = HashMap()
    private val byRefParameters: HashMap<IrDeclarationContainer, HashMap<IrFunction, ArrayList<IrValueParameter>>> = HashMap()
    private val filesToBeTransformed: HashSet<IrFile> = HashSet()

    private val allStructTypes: Map<IrDeclarationContainer, ArrayList<IrClass>>
        get() = structTypes + unionTypes

    fun isStruct(type: IrType): Boolean = structTypes.any { v -> v.value.any { it.defaultType == type } }
    fun isUnion(type: IrType): Boolean = unionTypes.any { v -> v.value.any { it.defaultType == type } }
    fun isStructOrUnion(type: IrType): Boolean = isStruct(type) || isUnion(type)

    fun needsTransformation(file: IrFile): Boolean = file in filesToBeTransformed

    fun discoverStructs(file: IrFile) {
        file.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                if (declaration.isClassWithFqName(KleaverNames.Interfaces.struct)
                        || declaration.isClassWithFqName(KleaverNames.Interfaces.union)) return
                for (iface in declaration.implementedInterfaces) {
                    val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: continue
                    if (iface.isClassWithFqName(KleaverNames.Interfaces.union)) {
                        unionTypes.getOrPut(container) { ArrayList() } += declaration
                        KleaverLog.info { "Found Union type ${declaration.name} in ${container.kotlinFqName}" }
                        continue
                    }
                    if (!iface.isClassWithFqName(KleaverNames.Interfaces.struct)) continue
                    structTypes.getOrPut(container) { ArrayList() } += declaration
                    KleaverLog.info { "Found Struct type ${declaration.name} in ${container.kotlinFqName}" }
                }
                super.visitClass(declaration)
            }
        })
    }

    fun discoverStructFields(file: IrFile) {
        file.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitProperty(declaration: IrProperty) {
                val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: return
                // Don't look for struct fields that need unrolling inside other structs
                if (container is IrClass && isStructOrUnion(container.defaultType)) return
                declaration.backingField?.let { field ->
                    if (!isStructOrUnion(field.type)) return@let
                    KleaverLog.info { "Found struct field ${declaration.name} in ${container.kotlinFqName}" }
                    structFields.getOrPut(container) { ArrayList() } += declaration
                    filesToBeTransformed += file
                }
                super.visitProperty(declaration)
            }
        })
    }

    fun discoverByRefParameters(file: IrFile) {
        file.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunction(declaration: IrFunction) {
                for (param in declaration.valueParameters) {
                    if (!param.hasAnnotation(KleaverNames.Annotations.byRef)) continue
                    val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: continue
                    byRefParameters.getOrPut(container) { HashMap() }.getOrPut(declaration) { ArrayList() } += param
                    KleaverLog.info { "Found ByRef value parameter ${declaration.name}#${param.name} in ${container.kotlinFqName}" }
                    filesToBeTransformed += file
                }
                super.visitFunction(declaration)
            }
        })
    }
}