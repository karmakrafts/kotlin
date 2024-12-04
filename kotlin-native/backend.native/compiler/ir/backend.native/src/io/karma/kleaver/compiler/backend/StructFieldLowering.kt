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
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.Name
import java.util.*

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal class StructFieldLoweringTransformer(
        private val state: NativeGenerationState
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

    private fun IrBuilderWithScope.getFieldInitializer(field: IrField): IrExpression {
        return createDefaultValue(field.type)
    }

    private fun unrollPropertyRecursively(
            rootProperty: IrProperty,
            type: IrClass,
            rootContainer: IrDeclarationContainer,
            parentNameStack: Stack<String>
    ) {
        val parentClass = rootContainer as? IrClass
        type.properties.filter(::isValidStructProperty).forEach { property ->
            val backingField = property.backingField ?: return@forEach
            val fieldType = backingField.type

            if (!LoweringAnalyzer.isStructOrUnion(fieldType)) {
                val propertyName = property.name.identifier
                val parentName = with(KleaverMangler) { parentNameStack.joinAndMangle() }
                val fieldName = with(KleaverMangler) { parentName and propertyName }
                val isMutable = rootProperty.isVar

                propertiesToAdd.getOrPut(rootContainer) { ArrayList() } += state.context.irFactory.buildProperty {
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
                        if (!property.isLateinit) {
                            initializer = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                                irExprBody(getFieldInitializer(backingField))
                            }
                        }
                    }
                    addGetter {
                        returnType = fieldType
                        startOffset = SYNTHETIC_OFFSET
                        endOffset = SYNTHETIC_OFFSET
                        visibility = property.getter?.visibility ?: DescriptorVisibilities.PRIVATE
                    }.apply {
                        parentClass?.let { dispatchReceiverParameter = it.thisReceiver!!.copyTo(this) } // Capture this-ref if parent is class
                        body = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody(this) {
                            +irReturn(irGetField(dispatchReceiverParameter?.let { irGet(it) }, this@property.backingField!!))
                        }
                    }
                    // If the original property is mutable, we need a setter too
                    if (isMutable) {
                        addSetter {
                            returnType = state.context.irBuiltIns.unitType
                            startOffset = SYNTHETIC_OFFSET
                            endOffset = SYNTHETIC_OFFSET
                            visibility = property.setter?.visibility ?: DescriptorVisibilities.PRIVATE
                        }.apply {
                            parentClass?.let { dispatchReceiverParameter = it.thisReceiver!!.copyTo(this) } // Capture this-ref if parent is class
                            val valueParam = addValueParameter("value", fieldType)
                            body = state.context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody(this) {
                                +irSetField(dispatchReceiverParameter?.let { irGet(it) }, this@property.backingField!!, irGet(valueParam))
                                +irReturnUnit()
                            }
                        }
                    }
                }
                KleaverLog.info { "Unrolled flat field into $fieldName" }
                return@forEach
            }

            val structTypeClass = fieldType.getClass() ?: return@forEach
            with(KleaverMangler) {
                parentNameStack.push(structTypeClass.mangledName())
            }
            unrollPropertyRecursively(rootProperty, structTypeClass, rootContainer, parentNameStack)
            parentNameStack.pop()
        }
    }

    private fun unrollProperty(property: IrProperty) {
        val container = property.parentClassOrNull ?: property.parentFileOrNull ?: return
        if (container is IrClass && LoweringAnalyzer.isStructOrUnion(container.defaultType)) return

        val field = property.backingField ?: return
        val fieldType = field.type
        if (!LoweringAnalyzer.isStructOrUnion(fieldType)) return
        val structTypeClass = fieldType.getClass() ?: return

        val parentNameStack = Stack<String>()
        with(KleaverMangler) {
            parentNameStack.push(field.mangledName())
        }
        unrollPropertyRecursively(property, structTypeClass, container, parentNameStack)

        propertiesToRemove.getOrPut(container) { ArrayList() } += property // Track which fields we have lowered
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitFile(declaration: IrFile) {
        super.visitFile(declaration)
        // Process globals
        declaration.properties
                .filter(::isValidStructProperty)
                .forEach(::unrollProperty)
    }

    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        // If the current class is a struct or union itself, don't unroll its properties
        if (LoweringAnalyzer.isStructOrUnion(declaration.defaultType)) return
        // Process member fields
        declaration.properties
                .filter(::isValidStructProperty)
                .forEach(::unrollProperty)
    }
}

internal fun transformStructFields(state: NativeGenerationState, file: IrFile) {
    if (!LoweringAnalyzer.needsTransformation(file)) return
    KleaverLog.info { "Running StructFieldLoweringTransformer for ${file.path}" }
    StructFieldLoweringTransformer(state).apply {
        file.acceptVoid(this)
        updateDeclarations()
    }
}