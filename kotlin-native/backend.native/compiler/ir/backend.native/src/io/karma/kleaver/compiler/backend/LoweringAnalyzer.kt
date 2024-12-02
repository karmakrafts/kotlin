/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.ir.implementedInterfaces
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal object LoweringAnalyzer {
    val structTypes: HashMap<IrFile, ArrayList<IrClass>> = HashMap()
    val unionTypes: HashMap<IrFile, ArrayList<IrClass>> = HashMap()
    val byRefParameters: HashMap<IrFunction, ArrayList<IrValueParameter>> = HashMap()
    val structFields: HashMap<IrFile, ArrayList<IrProperty>> = HashMap()
    val filesToBeTransformed: ArrayList<IrFile> = ArrayList()

    val allStructTypes: Map<IrFile, ArrayList<IrClass>>
        get() = structTypes + unionTypes

    fun isStruct(type: IrType): Boolean = structTypes.any { v -> v.value.any { it.defaultType == type } }
    fun isUnion(type: IrType): Boolean = unionTypes.any { v -> v.value.any { it.defaultType == type } }
    fun isStructOrUnion(type: IrType): Boolean = isStruct(type) || isUnion(type)

    fun needsTransformation(file: IrFile): Boolean = file in filesToBeTransformed

    fun discoverStructs(file: IrFile) {
        file.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitClass(declaration: IrClass) {
                if (declaration.isClassWithFqName(KleaverNames.Interfaces.struct)
                        || declaration.isClassWithFqName(KleaverNames.Interfaces.union)) return
                for (iface in declaration.implementedInterfaces) {
                    val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: continue
                    if (iface.isClassWithFqName(KleaverNames.Interfaces.union)) {
                        unionTypes.getOrPut(file) { ArrayList() } += declaration
                        KleaverLog.info { "Found Union type ${declaration.name} in ${container.kotlinFqName}" }
                        filesToBeTransformed += file
                        continue
                    }
                    if (!iface.isClassWithFqName(KleaverNames.Interfaces.struct)) continue
                    structTypes.getOrPut(file) { ArrayList() } += declaration
                    KleaverLog.info { "Found Struct type ${declaration.name} in ${container.kotlinFqName}" }
                    filesToBeTransformed += file
                }
                super.visitClass(declaration)
            }
        })
    }

    fun discoverStructFields(file: IrFile) {
        file.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitProperty(declaration: IrProperty) {
                declaration.backingField?.let { field ->
                    if (!isStructOrUnion(field.type)) return@let
                    val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: return@let
                    KleaverLog.info { "Found struct field ${declaration.name} in ${container.kotlinFqName}" }
                    structFields.getOrPut(file) { ArrayList() } += declaration
                }
                super.visitProperty(declaration)
            }
        })
    }

    fun discoverByRefParameters(file: IrFile) {
        file.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitFunction(declaration: IrFunction) {
                for (param in declaration.valueParameters) {
                    if (!param.hasAnnotation(KleaverNames.Annotations.byRef)) continue
                    val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: continue
                    byRefParameters.getOrPut(declaration) { ArrayList() } += param
                    KleaverLog.info { "Found ByRef value parameter ${declaration.name}#${param.name} in ${container.kotlinFqName}" }
                    filesToBeTransformed += file
                }
                super.visitFunction(declaration)
            }
        })
    }
}