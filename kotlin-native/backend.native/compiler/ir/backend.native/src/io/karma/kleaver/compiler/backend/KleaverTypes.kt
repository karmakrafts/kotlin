/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.backend.konan.InteropFqNames
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 05/12/2024
 */
internal class KleaverTypes(
        private val state: NativeGenerationState
) {
    private val builtins = state.context.irBuiltIns
    private val classCache: HashMap<FqName, IrClass> = HashMap()

    private fun findClassRecursively(container: IrDeclarationContainer, name: FqName): IrClass? {
        val children = container.declarations
        for (child in children.filterIsInstance<IrDeclarationContainer>()) {
            val clazz = findClassRecursively(child, name) ?: continue
            return clazz
        }
        return children.filterIsInstance<IrClass>()
                .find { it.fqNameWhenAvailable == name }
    }

    fun findClassOrNull(name: FqName): IrClass? {
        var clazz = classCache[name]
        if (clazz == null) {
            clazz = state.context.irModules.values
                    .flatMap { module ->
                        module.files.map { findClassRecursively(it, name) }
                    }
                    .first { it != null }
            if (clazz != null) classCache[name] = clazz
        }
        return clazz
    }

    fun findClass(name: FqName): IrClass = requireNotNull(findClassOrNull(name)) { "Could not find class named '$name'" }

    val byRefAnnotation: IrClass = findClass(KleaverNames.Annotations.byRef)
    val nativeMemUtils: IrClass = findClass(KleaverNames.Classes.nativeMemUtils)
    val cPointer: IrClass = findClass(InteropFqNames.cPointer.toSafe())
    val cPointed: IrClass = findClass(InteropFqNames.cPointed)
    val nativePtr: IrClass = findClass(KleaverNames.Classes.nativePtr)
    val nativePointed: IrClass = findClass(InteropFqNames.nativePointed.toSafe())

    val allocaFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("alloca"))

    val putByteFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putByte"))
    val putShortFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putShort"))
    val putIntFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putInt"))
    val putLongFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putLong"))
    val putFloatFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putFloat"))
    val putDoubleFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putDouble"))
    val putNativePtrFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("putNativePtr"))

    val getByteFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getByte"))
    val getShortFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getShort"))
    val getIntFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getInt"))
    val getLongFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getLong"))
    val getFloatFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getFloat"))
    val getDoubleFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getDouble"))
    val getNativePtrFunction: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("getNativePtr"))

    val copyMemoryFunction32: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("copyMemory"), listOf(
            nativePtr.defaultType,
            builtins.intType,
            nativePtr.defaultType
    ))
    val copyMemoryFunction64: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("copyMemory"), listOf(
            nativePtr.defaultType,
            builtins.longType,
            nativePtr.defaultType
    ))

    val moveMemoryFunction32: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("moveMemory"), listOf(
            nativePtr.defaultType,
            builtins.intType,
            nativePtr.defaultType
    ))
    val moveMemoryFunction64: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("moveMemory"), listOf(
            nativePtr.defaultType,
            builtins.longType,
            nativePtr.defaultType
    ))

    val setMemoryFunction32: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("setMemory"), listOf(
            nativePtr.defaultType,
            builtins.byteType,
            builtins.intType
    ))
    val setMemoryFunction64: IrSimpleFunction = nativeMemUtils.findFunction(Name.identifier("setMemory"), listOf(
            nativePtr.defaultType,
            builtins.byteType,
            builtins.longType
    ))
}