/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.ir.implementedInterfaces
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 01/12/2024
 */

internal inline fun List<IrModuleFragment>.forEachFile(closure: (IrFile) -> Unit) {
    for (module in this) {
        for (file in module.files) {
            closure(file)
        }
    }
}

internal fun IrDeclarationContainer.findPropertyOrNull(name: Name): IrProperty? =
        properties.find { it.name == name }

internal fun IrDeclarationContainer.findProperty(name: Name): IrProperty =
        requireNotNull(findPropertyOrNull(name)) { "Could not find property '$name' " }

internal fun IrDeclarationContainer.findFieldOrNull(name: Name): IrField? = declarations
        .filterIsInstance<IrField>()
        .find { it.name == name }
        ?: properties
                .find { it.name == name && it.backingField != null }
                ?.backingField

internal fun IrDeclarationContainer.findField(name: Name): IrField =
        requireNotNull(findFieldOrNull(name)) { "Could not find field '$name' in $kotlinFqName" }

internal fun IrDeclarationContainer.findFunctionOrNull(name: Name, argTypes: List<IrType>? = null): IrSimpleFunction? {
    if (argTypes == null) {
        return declarations
                .filterIsInstance<IrSimpleFunction>()
                .find { it.name == name }
    }
    return declarations
            .filterIsInstance<IrSimpleFunction>()
            .find {
                it.name == name && it.valueParameters.map { param -> param.type } == argTypes
            }
}

internal fun IrDeclarationContainer.findFunction(name: Name, argTypes: List<IrType>? = null): IrSimpleFunction =
        requireNotNull(findFunctionOrNull(name, argTypes)) { "Could not find function '$name' in $kotlinFqName" }

internal val IrDeclaration.parentFileOrNull: IrFile?
    get() = parent.let {
        when (it) {
            is IrFile -> it
            is IrDeclaration -> it.parentFileOrNull
            else -> null
        }
    }

internal val IrClass.isStruct: Boolean
    get() = implementedInterfaces.any { it.isClassWithFqName(KleaverNames.Interfaces.struct) }

internal val IrType.isStruct: Boolean
    get() = getClass()?.isStruct ?: false

internal fun IrType.unroll(): List<IrType> = when {
    isStruct -> getClass()!!.properties
            .filter { it.backingField != null }
            .flatMap { it.backingField!!.type.unroll() }
            .toList()
    else -> listOf(this)
}

internal fun IrClass.unroll(): List<IrType> = defaultType.unroll()

internal fun IrElementBuilder.synthetic() {
    startOffset = SYNTHETIC_OFFSET
    endOffset = SYNTHETIC_OFFSET
}

internal fun IrBuilderWithScope.irFloat(value: Float): IrConst<*> =
        IrConstImpl.float(startOffset, endOffset, context.irBuiltIns.floatType, value)

internal fun IrBuilderWithScope.irDouble(value: Double): IrConst<*> =
        IrConstImpl.double(startOffset, endOffset, context.irBuiltIns.floatType, value)

internal fun IrBuilderWithScope.createDefaultValue(
        type: IrType
): IrExpression = when {
    type.isByte() -> irByte(0)
    type.isShort() -> irShort(0)
    type.isInt() -> irInt(0)
    type.isLong() -> irLong(0)
    type.isFloat() -> irFloat(0F)
    type.isDouble() -> irDouble(0.0)
    type.isBoolean() -> irBoolean(false)
    type.isString() -> irString("")
    else -> throw IllegalStateException("No constant default value for type $this")
}