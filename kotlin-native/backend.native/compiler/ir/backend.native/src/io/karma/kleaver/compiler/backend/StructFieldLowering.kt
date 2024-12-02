/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr.signatureString
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import java.util.*

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal class StructFieldLoweringTransformer(
        private val state: NativeGenerationState
) : IrElementTransformerVoid() {
    companion object {
        private const val STRUCT_FIELD_PREFIX: String = "__kleaver_struct"

        private fun IrDeclarationContainer.mangledName(): String = when (this) {
            is IrClass -> signatureString(true)
            is IrFile -> name.replace('.', '_')
            else -> throw IllegalStateException("Unexpected declaration container type")
        }
    }

    internal val properties: ArrayList<IrProperty> = ArrayList()

    private fun isValidStructProperty(property: IrProperty): Boolean {
        if (property.isConst || property.isExternal || property.isDelegated) return false
        val backingField = property.backingField ?: return false
        return !backingField.isExternal
    }

    private fun unrollPropertyRecursively(
            rootProperty: IrProperty,
            type: IrClass,
            rootContainer: IrDeclarationContainer,
            parentNameStack: Stack<String> = Stack<String>()
    ) {
        val props = type.properties.filter(::isValidStructProperty)
        for (property in props) {
            val container = property.parentClassOrNull ?: property.parentFileOrNull ?: return
            val backingField = property.backingField ?: continue
            val fieldType = backingField.type

            if (!LoweringAnalyzer.isStructOrUnion(fieldType)) {
                val propertyName = property.name
                val parentName = (parentNameStack + container.mangledName()).joinToString(separator = "_")
                val fieldName = Name.identifier("${STRUCT_FIELD_PREFIX}_${parentName}_$propertyName")
                rootContainer.addChild(state.context.irFactory.buildProperty {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    name = fieldName
                    modality = Modality.FINAL
                    visibility = rootProperty.visibility
                    isVar = rootProperty.isVar
                    isLateinit = property.isLateinit
                }.apply {
                    copyAnnotationsFrom(rootProperty)
                    copyAnnotationsFrom(property)
                    parent = rootContainer
                    addBackingField {
                        startOffset = SYNTHETIC_OFFSET
                        endOffset = SYNTHETIC_OFFSET
                        name = fieldName
                        this.type = fieldType
                        this.isStatic = rootProperty.backingField!!.isStatic
                        isFinal = !property.isVar
                        isLateinit = property.isLateinit
                        visibility = rootProperty.visibility
                    }.apply {
                        // Shallow-copy initializer into a new expression body
                        initializer = state.context.irFactory.createExpressionBody(fieldType.createDefaultValue())
                    }
                })
                KleaverLog.info { "Unrolled flat field ${container.kotlinFqName}#$propertyName into $fieldName" }
                continue
            }
            val structTypeClass = fieldType.getClass() ?: continue

            parentNameStack.push(structTypeClass.mangledName())
            unrollPropertyRecursively(rootProperty, structTypeClass, rootContainer, parentNameStack)
            parentNameStack.pop()
            KleaverLog.info { "Unrolled nested field $${container.kotlinFqName}#${property.name}" }
        }
    }

    private fun unrollProperty(property: IrProperty) {
        val field = property.backingField ?: return
        val fieldType = field.type
        if (!LoweringAnalyzer.isStructOrUnion(fieldType)) return

        val container = field.parentClassOrNull ?: field.parentFileOrNull ?: return

        val structTypeClass = fieldType.getClass() ?: return
        KleaverLog.info { "Unrolling struct field ${container.kotlinFqName}#${field.name}" }

        unrollPropertyRecursively(property, structTypeClass, container)
        properties += property // Track which fields we have lowered
    }

    override fun visitProperty(declaration: IrProperty): IrStatement {
        if (isValidStructProperty(declaration)) {
            unrollProperty(declaration)
        }
        return super.visitProperty(declaration)
    }
}

internal fun transformStructFields(state: NativeGenerationState, file: IrFile) {
    if (!LoweringAnalyzer.needsTransformation(file)) return
    KleaverLog.info { "Running StructFieldLoweringTransformer for ${file.path}" }
    file.transformChildrenVoid(StructFieldLoweringTransformer(state))
}