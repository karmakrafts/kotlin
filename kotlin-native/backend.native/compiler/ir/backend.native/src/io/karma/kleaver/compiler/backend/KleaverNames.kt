/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
object KleaverNames {
    val runtimePackageName: FqName = FqName("io.karma.kleaver.runtime")
    val compilerPackageName: FqName = FqName("io.karma.kleaver.compiler")

    object Interfaces {
        val struct: FqName = ClassId(runtimePackageName, Name.identifier("Struct")).asSingleFqName()
        val union: FqName = ClassId(runtimePackageName, Name.identifier("Union")).asSingleFqName()
    }

    object Annotations {
        val fieldOffset: FqName = ClassId(compilerPackageName, Name.identifier("FieldOffset")).asSingleFqName()
        val byRef: FqName = ClassId(compilerPackageName, Name.identifier("ByRef")).asSingleFqName()
        val alignAs: FqName = ClassId(compilerPackageName, Name.identifier("AlignAs")).asSingleFqName()
        val bitField: FqName = ClassId(compilerPackageName, Name.identifier("BitField")).asSingleFqName()
    }
}