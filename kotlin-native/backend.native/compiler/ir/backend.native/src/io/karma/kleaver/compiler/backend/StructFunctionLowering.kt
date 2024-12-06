/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.ir.buildSimpleAnnotation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOriginImpl
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 01/12/2024
 */

internal val KLEAVER_RETURN_ADDRESS_PARAM: IrDeclarationOriginImpl = IrDeclarationOriginImpl("KLEAVER_RETURN_ADDRESS_PARAM", true)
internal val KLEAVER_RETURN_ADDRESS_LOCAL: IrStatementOriginImpl = IrStatementOriginImpl("KLEAVER_RETURN_ADDRESS_LOCAL")

// Lower all functions that return structures to functions which use a pointer to the parent stack frame via @ByRef
internal class StructReturnLoweringTransformer(
        private val state: NativeGenerationState,
        private val types: KleaverTypes,
) : IrElementVisitorVoid {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitFunction(declaration: IrFunction) {
        super.visitFunction(declaration)
        val type = declaration.returnType
        if (!type.isStruct) return
        declaration.returnType = state.context.irBuiltIns.unitType // Return type becomes unit
        // Last parameter becomes a pointer to the structure to be returned
        declaration.addValueParameter {
            index = 0
            name = with(KleaverMangler) { Name.identifier("return".mangledParameterName) }
            this.type = type // Return type becomes parameter type
            origin = KLEAVER_RETURN_ADDRESS_PARAM
            synthetic()
        }.apply {
            annotations += buildSimpleAnnotation(
                    state.context.irBuiltIns,
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    types.byRefAnnotation
            )
        }
        val container = declaration.parentClassOrNull ?: declaration.parentFileOrNull ?: return
        KleaverLog.info { "Transformed struct return type of ${container.kotlinFqName}#${declaration.name}" }
    }
}

// Lower all calls to functions that return structures into blocks which stack-allocate a temporary struct
internal class StructReturnCallLoweringTransformer(
        private val state: NativeGenerationState,
        private val types: KleaverTypes,
) : IrElementTransformerVoid() {
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitCall(expression: IrCall): IrExpression {
        super.visitCall(expression)
        val target = expression.symbol.owner
        // Only transform calls whose target functions last parameter is a structure type and annotated with @ByRef
        val lastParam = target.valueParameters.first()
        if (!lastParam.type.isStruct || !lastParam.hasAnnotation(KleaverNames.Annotations.byRef)) return expression
        return expression
    }
}

internal fun transformStructFunctions(state: NativeGenerationState, file: IrFile, types: KleaverTypes) {
    file.acceptVoid(StructReturnLoweringTransformer(state, types))
    file.transform(StructReturnCallLoweringTransformer(state, types), null)
}