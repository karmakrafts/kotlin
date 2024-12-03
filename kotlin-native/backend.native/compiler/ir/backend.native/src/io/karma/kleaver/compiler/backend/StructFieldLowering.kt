/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.Name
import java.util.*

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal class StructFieldLoweringTransformer(
        private val state: NativeGenerationState,
        private val isTopLevel: Boolean
) : IrElementVisitorVoid {
    private val propertiesToAdd: HashMap<IrDeclarationContainer, ArrayList<IrProperty>> = HashMap()
    private val propertiesToRemove: HashMap<IrDeclarationContainer, ArrayList<IrProperty>> = HashMap()

    internal fun updateDeclarations() {
        propertiesToAdd.forEach { (container, declarations) ->
            container.addChildren(declarations)
        }
        propertiesToRemove.forEach { (container, declarations) ->
            container.declarations -= declarations.toSet()
        }
    }

    private fun isValidStructProperty(property: IrProperty): Boolean {
        if (property.isConst || property.isExternal || property.isDelegated) return false
        val backingField = property.backingField ?: return false
        return !backingField.isExternal
    }

    private fun unrollPropertyRecursively(
            rootProperty: IrProperty,
            type: IrClass,
            rootContainer: IrDeclarationContainer,
            parentNameStack: Stack<String>
    ) {
        type.properties.filter(::isValidStructProperty).forEach { property ->
            val container = property.parentClassOrNull ?: property.parentFileOrNull ?: return@forEach
            val backingField = property.backingField ?: return@forEach
            val fieldType = backingField.type

            if (!LoweringAnalyzer.isStructOrUnion(fieldType)) {
                val propertyName = property.name.identifier
                val parentName = parentNameStack.joinToString(KleaverMangler.NAME_DELIMITER)
                val fieldName = with(KleaverMangler) { parentName and propertyName }
                val isMutable = rootProperty.isVar
                // This-spam is to avoid scope confusion
                propertiesToAdd.getOrPut(container) { ArrayList() } += state.context.irFactory.buildProperty {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    name = requireNotNull(Name.identifierIfValid(fieldName)) { "Field name '$fieldName' is invalid" }
                    modality = Modality.FINAL
                    visibility = rootProperty.visibility
                    isVar = isMutable
                    isLateinit = property.isLateinit
                }.apply property@{
                    copyAnnotationsFrom(rootProperty)
                    copyAnnotationsFrom(property)
                    parent = rootContainer
                    addBackingField {
                        startOffset = SYNTHETIC_OFFSET
                        endOffset = SYNTHETIC_OFFSET
                        this.type = fieldType
                        isStatic = rootProperty.backingField!!.isStatic
                        isFinal = !isMutable
                        visibility = DescriptorVisibilities.PRIVATE // Do we need to inherit this?
                    }.apply {
                        // Shallow-copy initializer expression into a new expression body
                        initializer = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                            // TODO: process initializers
                            irExprBody(createDefaultValue(fieldType))
                        }
                    }
                    addGetter {
                        returnType = fieldType
                        startOffset = SYNTHETIC_OFFSET
                        endOffset = SYNTHETIC_OFFSET
                        visibility = property.getter?.visibility ?: DescriptorVisibilities.PRIVATE
                    }.apply {
                        body = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody(this) {
                            +irReturn(irGetField(null, this@property.backingField!!))
                        }
                    }
                    if (isMutable) { // If the original property is mutable, we need a setter too
                        addSetter {
                            returnType = state.context.irBuiltIns.unitType
                            startOffset = SYNTHETIC_OFFSET
                            endOffset = SYNTHETIC_OFFSET
                            visibility = property.setter?.visibility ?: DescriptorVisibilities.PRIVATE
                        }.apply {
                            val valueParam = addValueParameter("value", fieldType)
                            body = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody(this) {
                                +irSetField(null, this@property.backingField!!, irGet(valueParam))
                                +irReturnUnit()
                            }
                        }
                    }
                }
                KleaverLog.info { "Unrolled flat field ${container.kotlinFqName}#$propertyName into $fieldName" }
                return@forEach
            }
            val structTypeClass = fieldType.getClass() ?: return@forEach

            with(KleaverMangler) {
                parentNameStack.push(structTypeClass.mangledName())
            }
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

        val parentNameStack = Stack<String>()
        with(KleaverMangler) {
            parentNameStack.push(field.mangledName())
        }
        unrollPropertyRecursively(property, structTypeClass, container, parentNameStack)

        propertiesToRemove.getOrPut(container) { ArrayList() } += property // Track which fields we have lowered
    }

    override fun visitProperty(declaration: IrProperty) {
        if (isValidStructProperty(declaration) && declaration.isTopLevel == isTopLevel) {
            unrollProperty(declaration)
        }
        super.visitProperty(declaration)
    }
}

internal fun transformStructFields(state: NativeGenerationState, file: IrFile) {
    if (!LoweringAnalyzer.needsTransformation(file)) return
    KleaverLog.info { "Running StructFieldLoweringPreTransformer for ${file.path}" }
    // Transform globals
    StructFieldLoweringTransformer(state, true).apply {
        file.acceptChildrenVoid(this)
        updateDeclarations() // Remove residual global declarations once we're done with this file
    }
}