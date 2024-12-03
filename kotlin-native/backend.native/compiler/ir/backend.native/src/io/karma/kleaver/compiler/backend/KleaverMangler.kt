/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

/**
 * @author Alexander Hinze
 * @since 03/12/2024
 */
internal object KleaverMangler {
    private const val KLEAVER_PREFIX: String = "klv"
    private const val STRUCT_OBJECT_PREFIX: String = "__${KLEAVER_PREFIX}sob__"
    private const val STRUCT_FIELD_PREFIX: String = "__${KLEAVER_PREFIX}sfd__"
    private const val STRUCT_FUNCTION_PREFIX: String = "__${KLEAVER_PREFIX}sfn__"
    private const val ARRAY_PREFIX: String = "A$"
    private const val NULLABLE_PREFIX: String = "N$"
    internal const val NAME_DELIMITER: String = "_"

    infix fun String.and(value: String): String = when {
        isBlank() -> value
        value.isBlank() -> this
        else -> "${this}$NAME_DELIMITER$value"
    }

    fun FqName.mangle(): String = toString().replace(".", NAME_DELIMITER)

    fun IrClass.mangledName(): String = kotlinFqName.mangle()

    fun IrType.mangledName(): String {
        var name = ""
        if (isArray()) name += ARRAY_PREFIX
        if (isNullable()) name += NULLABLE_PREFIX
        if (isPrimitiveType()) {
            name += when { // Thank you Java
                isByte() -> "B"
                isShort() -> "S"
                isInt() -> "I"
                isLong() -> "J"
                isFloat() -> "F"
                isDouble() -> "D"
                isBoolean() -> "Z"
                else -> throw IllegalStateException("Unrecognized primitive type '$this'")
            }
            return name
        }
        assert(this is IrSimpleType) { "Unrecognized IrType class '$this'" }
        val className = requireNotNull(getClass()?.kotlinFqName?.mangle()) { "Class must have a fully qualified name to be mangled" }
        return name and className
    }

    fun IrField.mangledName(): String {
        return "$STRUCT_FIELD_PREFIX${name.identifier}" and type.mangledName()
    }
}